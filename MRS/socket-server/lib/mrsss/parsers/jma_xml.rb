# coding: UTF-8

module Mrsss
  module Parsers
  
    #
    # JMAから受信したXMLデータを解析しRedmineへ登録するための処理を行うクラスです。
    #
    class JmaXml
    
      #
      # 初期化処理を行います。
      #
      # ==== Args
      # _mode_ :: 動作モード (0:通常,1:訓練,2:試験)
      # _channel_id_ :: 入力元識別子
      # ==== Return
      # ==== Raise
      def initialize(mode, channel_id)
        @mode = mode
        @channel_id = channel_id
        @log = Mrsss.parser_logger
        @send_redmine = false
      end
      
      #
      # 受信データの解析、Redmineへの送信処理を行います。
      #
      # ==== Args
      # _contents_ :: 受信データ
      # ==== Return
      # ==== Raise
      def handle(contents)
        
        @xml = Nokogiri::XML(contents)
        
        # スキーマチェック
        schema = Mrsss::get_jma_schema()
        if schema.valid?(@xml)
          @log.info("XMLスキーマチェック正常")
        else
          @log.error("XMLスキーマチェックエラーのため処理を中断します")
          raise RuntimeError.new("XMLスキーマチェックエラー")
        end
        
        # XMLの解析(データの解析を行うため、このなかでアクセス制限のフラグ[@send_redmine]を設定する)
        parse()
        
        # 送信電文作成
        issue_json = create_issue_json()

        if @send_redmine
          # Redmineへ送信
          Redmine::post_issues(issue_json)
        end 
      end


private

      #
      # issue登録用のJSONデータを作成する
      #
      def create_issue_json()

        # issueに登録するためのJSONデータ用Hash
        json = {}
        issue = {}
        json['issue'] = issue
        
        # プロジェクトID
        issue['project_id'] = @project_id
        # トラッカーID
        issue['tracker_id'] = @tracker_id
        # XML_Control
        issue['xml_control'] = @xml_control
        # XML_Head
        issue['xml_head'] = @xml_head
        # XML_Body
        issue['xml_body'] = @xml_body
        
        # issue_extras
        @issue_extras.each { |param, value|
          unless value.blank?
            issue[param] = value
          end
        }
        
        # issue_geographies
        issue['issue_geographies'] = @issue_geographies
        
        # send_target
        disposition_number = @disposition_number
        # 配備番号がある場合はメッセージと自動送信フラグを取得
        unless disposition_number.blank?
          issue['send_target'] = @disposition_number
          
          # 自動送信の判定
          if @is_autosend == true
            issue['auto_send'] = '1'
            
            # 自動送信時の拡張パラメータ
            @autosend_extras.each { |param, value|
            unless value.blank?
              issue[param] = value
            end
          }
          end
          
          # メッセージ
          unless @send_message.empty?
            @send_message.each { |key, value|
              unless value.blank?
                issue[key] = value
              end
            }
          end
        end
        
        # 自動プロジェクト立ち上げの判定
        if @is_autolaunch == true
          issue['auto_launch'] = '1'
        end

        # 情報種別取得パラメータ
        description_target_param = @@rule['description_target_param']
        issue[description_target_param] = @description

        log_str = "[#{@channel_id}] 送信JSONデータ\n"
        log_str = "#{log_str}--------------------------------------------------------------------------------\n"
        log_str = "#{log_str}#{json}\n"
        log_str = "#{log_str}--------------------------------------------------------------------------------"
        @log.debug(log_str)

        json.to_json
      end

      #
      # XMLの解析処理
      #
      def parse()
        
        # 名前空間を無効にする!
        # 無効にしないとXPathで値を取得できない
        @xml.remove_namespaces!
        
        # 解析ルールをロード
        @@rule ||= YAML.load(File.open(File.join(Util.get_config_path(__FILE__), "jma_xml_parse_rule.yml")))
        
        raise RuntimeError.new('JMAのXML解析用設定ファイルがロードできませんでした。ファイルを確認してください。') if @@rule.blank?
        
        # ---------------------------------------------------------------
        # XMLの解析作業開始
        # ---------------------------------------------------------------
        # xml_controlの設定
        @xml_control = @xml.xpath(@@rule['xml_control']).to_s
        # xml_headの設定
        @xml_head = @xml.xpath(@@rule['xml_head']).to_s
        # xml_bodyの設定
        @xml_body = @xml.xpath(@@rule['xml_body']).to_s
        
        # トラッカーIDの設定
        @tracker_id = tracker_id()
        
        # プロジェクトIDの設定
        if @mode == 1
          # mode:1(訓練モード)の場合は訓練モード専用のプロジェクトID設定
          config = Mrsss::get_mrsss_config
          @project_id = config['trainingmode_project_id']
        elsif @mode == 2
          # mode:2(試験モード)の場合は試験モード専用のプロジェクトID設定
          config = Mrsss::get_mrsss_config
          @project_id = config['testmode_project_id']
        else
          # mode:0(通常モード)の場合はXML内の通常/訓練/試験モードを設定
          @project_id = project_id()
        end
        
        # issue拡張フィールドの設定
        @issue_extras = issue_extras()
        # 地理情報の設定
        @issue_geographies = issue_geographies()
        # 配備番号の設定
        @disposition_number = disposition_number()
        # 送信メッセージの設定
        @send_message = send_message()
        # 自動送信有無の設定
        @is_autosend = is_autosend()  # disposition_number()実施後に実施する必要あり
        # 自動立ち上げ有無の設定
        @is_autolaunch = is_autolaunch()
        # 自動送信付加情報の設定
        # 内容はXMLの解析ではなく設定ファイルから取得
        @autosend_extras = @@rule['autosend_extras']
        # 情報種別を取得
        @description = parse_description()
        # アクセス制限フラグ
        @send_redmine = parse_access_limit()
        
      end

      def parse_access_limit()
        found = false

        # 情報種別を取得
        description_type_path = @@rule['description_type_path']

        # 説明文
        contents = @xml.xpath(description_type_path).to_s
        description_contents = @@rule['forward_to_issue'][contents]
        if description_contents.nil?
          return false
        end

        description_contents.each do |entry|
          # xpathにより情報を取得
          value = @xml.xpath(entry["path"]).to_s unless entry["path"].nil?
          if !value.blank?
            found = true
          end
        end

        return found
      end

      def parse_description()
        builder = ""

        # 情報種別を取得
        description_type_path = @@rule['description_type_path']
        # 説明文
        contents = @xml.xpath(description_type_path).to_s
        description_contents = @@rule['description_contents'][contents]
        if description_contents.nil?
          return ""
        end

        builder = ""
        description_contents.each do |entry|
          # xpathにより情報を取得
          value = @xml.xpath(entry["path"]).to_s unless entry["path"].nil?
          next if value.blank?
          value = entry["header"] + value unless entry["header"].nil?
    
          unless entry["decorator"].nil?
	    value = Mrsss::Parsers::ParseUtil.send(entry["decorator"],value)
          end

          builder << value
          unless entry["delimiter"].nil?
            builder << entry["delimiter"]
          else
            builder << "\n"
          end

          unless entry["child_nodes"].nil?
           children =  parse_children(entry, @xml)
           builder << children
          end
        end

        return builder
      end

      def parse_children(entry, jma_xml)
        blank = " "
        str = ""

        return "" if (entry["child_nodes"][0].nil? || entry["child_nodes"][0]["root"].nil?)
          
        jma_xml.xpath(entry["child_nodes"][0]["root"]).each do |root_path|
          entry["child_nodes"].each do |child_entry|
            unless child_entry["path"].nil?
              child_value = root_path.xpath(child_entry["path"]).to_s
              unless child_value.blank?
                str = str + blank + child_value
                str = str + "\n"
              end
            end

            if !(child_entry["child_nodes"].nil? || child_entry["child_nodes"][0].nil? || child_entry["child_nodes"][0]["root"].nil?)
              root_path.xpath(child_entry["child_nodes"][0]["root"]).each do |sibling_path|
                child_entry["child_nodes"].each do |sibling_entry|
                  unless sibling_entry["path"].nil?
                    sibling_value = sibling_path.xpath(sibling_entry["path"]).to_s
                    unless sibling_value.blank?
                      str = str + blank + blank + sibling_value
                      str = str + "\n"
                    end
                  end
                end
              end
            end          
          end
        end

        return str 
      end

      #
      # トラッカーIDを取得
      #
      def tracker_id
        # ルール設定ファイルには入力識別子単位で定義されている
        trackers_map = @@rule['trackers'][@channel_id]
        
        tracker_id = nil
        
        # トラッカーを決定するためのXPath取得
        location = trackers_map['path']
        unless location.blank?
          # XPathでタイトル取得
          title = @xml.xpath(location).to_s
          # typeHashからタイトルをキーにトラッカーIDを取得
          types = trackers_map['type']
          tracker_id = types[title]
        end
        
        # トラッカーIDが取得できない場合はデフォルトのトラッカーIDを取得
        if tracker_id.blank?
          tracker_id = trackers_map['default']
        end
        
        tracker_id
      end
      
      #
      # プロジェクトIDを取得
      #
      def project_id
        projects_map = @@rule['projects']
        location = projects_map['path']
        status = @xml.xpath(location).to_s
        types = projects_map['type']
        project_id = types[status]
        if project_id.blank?
          project_id = types['default']
        end
        
        project_id
      end
      
      #
      # issue_extrasに設定する値を取得
      #
      def issue_extras
        # 戻り値用ハッシュテーブル
        ret = {}
        issue_extras_map = @@rule['issue_extras']
        # issue_extras に定義されいるxpath数分ループ
        issue_extras_map.each do |key, location|
          # xpathを使用して値を取得
          val = @xml.xpath(location).to_s
          # 戻りハッシュテーブルに値を設定
          ret[key] = val
        end
        ret
      end
      
      #
      # 地理系情報を取得
      #
      def issue_geographies
        
        # 戻り値用変数
        issue_geographies = []
        
        # 解析ルール取得
        geographies = @@rule['issue_geographies']
        
        # 解析ルールのうちcoordinateを解析
        coordinate = geographies['coordinate']
        point_geographies = parse_issue_geography(coordinate, 'point')
        unless point_geographies.empty?
          issue_geographies.concat(point_geographies)
        end
        
        # 解析ルールのうちlineを解析
        line = geographies['line']
        line_geographies = parse_issue_geography(line, 'line')
        unless line_geographies.empty?
          issue_geographies.concat(line_geographies)
        end
        
        # 解析ルールのうちpolygonを解析
        polygon = geographies['polygon']
        polygon_geographies = parse_issue_geography(polygon, 'polygon')
        unless polygon_geographies.empty?
          issue_geographies.concat(polygon_geographies)
        end
        
        # 解析ルールのうちlocationを解析
        location = geographies['location']
        location_geographies = parse_issue_geography(location, 'location')

        unless location_geographies.empty?
          issue_geographies.concat(location_geographies)
        end
        issue_geographies
      end
      
      #
      # 地理系情報を解析
      # geokey -> 'coordinate'/'polygon'/'line'/'location'のいずれか
      #
      def parse_issue_geography(rules, geokey)
        
        issue_geographies = []
        
        rules.each do |item|
          
          # xpath
          location = item['path']
          # remarks
          remarks_path = item['remarks_path']
          # static_remarks
          static_remarks = item['static_remarks']
          # allow_type
          allow_type = item['allow_type']
          
          # xpathで取得できるNodeList全てに対して処理を行う
          @xml.xpath(location).each do |node|
            
            # 該当Nodeがtype属性を持ち、allow_typeの指定がある場合は一致するtypeを持つNodeの値のみ使用する
            type = node.xpath("../@type")[0].to_s
            unless type.blank?
              unless allow_type.blank?
                #Lgdisit.logger.debug("typeチェック => 許可タイプ:#{allow_type} 実際のタイプ:#{type}")
                # allow_typeの指定と異なるtype属性を持つNodeの場合は使用しない
                unless type == allow_type
                  # typeとallow_typeが一致しない場合は該当データスキップ
                  next
                end
              end
            end
            
            # 地理情報
            geo = node.to_s
            
            # 測地系情報
            datum = node.xpath("../@datum").to_s
            
            # ---------------------------------------------------------
            # 備考文字列
            # parseRuleにREMARKS_PATHSが設定されている場合は配列に設定
            # されているXpathを使用して備考文字列を取得する
            # 設定されていない場合はSTATIC_REMARKSを使用して備考文字列を取得する
            # ---------------------------------------------------------
            remarks = ''
            if static_remarks.blank?
              unless remarks_path.blank?
                remarks = node.xpath(remarks_path).to_s
              end
            else
              remarks = static_remarks
            end
            
            # 備考の最後にtypeも設定する
            unless type.blank?
              remarks = "#{remarks} #{type}"
            end
            
            # geokeyにより地理情報を変換してissue_geographyに格納
            issue_geography = {}
            if geokey == 'point'
              issue_geography_point = ParseUtil.convert_point(geo)
              @log.debug("point => #{issue_geography_point}")
              issue_geography['point'] = issue_geography_point
            elsif geokey == 'line'
              issue_geography_line = ParseUtil.convert_point_array(geo)
              @log.debug("line => #{issue_geography_line}")
              issue_geography['line'] = issue_geography_line
            elsif geokey == 'polygon'
              issue_geography_polygon = ParseUtil.convert_point_array(geo)
              @log.debug("polygon => #{issue_geography_polygon}")
              issue_geography['polygon'] = issue_geography_polygon
            elsif geokey == 'location'
              @log.debug("location => #{geo}")
              issue_geography['location'] = geo
            end
            

            # 備考情報を格納
            unless remarks.blank?
              issue_geography['remarks'] = remarks
            end

            # 測地系情報を格納
            unless datum.blank?
              issue_geography['datum'] = datum
            end

            # 戻り値用配列に地理系情報Hashを追加
            issue_geographies.push(issue_geography)
          end
        end
        issue_geographies
      end
      
      #
      # プロジェクト自動送信フラグを取得
      #
      def is_autosend()
        autosend_nos = @@rule['autosend_disposition_numbers']
        autosend_nos.include?(@disposition_number)
      end
      
      #
      # プロジェクト自動立ち上げフラグを取得
      #
      def is_autolaunch
        # 震度による判定
        autolaunch_map = @@rule['autolaunch']
        
        threshold = ParseUtil.intensity_to_f(autolaunch_map['earthquake_threashold']) # しきい値
        location = autolaunch_map['earthquake_path']  # 震度取得用xpath
        
        nodelist = @xml.xpath(location)
        nodelist.each do |node|
          node_val = ParseUtil.intensity_to_f(node.to_s)
          if node_val >= threshold
            return true
          end
        end
        
        # 津波の高さによる判定
        threshold = autolaunch_map['tsunami_threashold'] # しきい値
        location = autolaunch_map['tsunami_path']  # 震度取得用xpath
        
        nodelist = @xml.xpath(location)
        nodelist.each do |node|
          node_val = node.to_s.to_f
          if node_val >= threshold
            return true
          end
        end
        
        return false
      end
      
      #
      # 配備番号を取得
      #
      def disposition_number
        
        disposition_info = @@rule['disposition']
        
        disposition_info.each do |info|
          
          disposition_number = info['number']
          disposition_paths = info['paths']
          disposition_paths.each do |aPath|
            node = @xml.xpath(aPath)
            unless node.empty?
              return disposition_number
            end
          end
        end
        nil
      end
      
      #
      # 自動送信時のメッセージを取得
      #
      def send_message
        ret_messages = {}
        send_message_info = @@rule['send_message']
        send_message_info.each do |info|
          rest_parameter = info['parameter']
          max_length = info['max_length']
          paths = info['paths']
          message = ''
          paths.each do |path|
            txt = @xml.xpath(path).to_s
            unless txt.blank?
              if message.length == 0
                message = txt
              else
                message = message + ' ' + txt
              end
            end
          end
          unless message.blank?
            message = message.slice(0, max_length)
            ret_messages[rest_parameter] = message
          end
        end
        return ret_messages
      end
      
    end # JmaXml
  end # Parsers
end # Mrsss
