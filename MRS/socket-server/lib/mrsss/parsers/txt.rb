# encoding: utf-8

module Mrsss
  module Parsers
    
    #
    # J-Alertから受信したTxtファイルをRedmineへ登録するための処理を行うクラスです。
    #
    class Txt
  
      #
      # 初期化処理
      #
      # ==== Args
      # _mode_ :: 動作モード (0:通常, 1:訓練, 2:試験)
      # _channel_id_ :: 入力元識別子
      # ==== Return
      # ==== Raise
      def initialize(mode, channel_id)
        @mode = mode
        @channel_id = channel_id
        @log = Mrsss.parser_logger
      end
      
      #
      # TXTファイルのRedmineへの送信処理を行います。
      #
      # ==== Args
      # _contents_ :: Txtファイル名とファイル内容を保持したHash
      # ==== Return
      # ==== Raise
      def handle(contents)
        
        attributes = []
        
        # ファイル内容より通常/訓練モードを判定
        contents.each do |entry|
          name = entry['name']
          # 拡張子を確認
          ext = File.extname(name)
          # iniファイルの設定値を確認
          if ext == '.ini'
            file = entry['file']
            inifile = IniFile.new(file)
            # Consts000セクションのTrainingFlgに設定されている値が'0'の場合は訓練モード
            flg = inifile['Contents000']['TrainingFlg']
            if flg == '0' && @mode == 0
              @log.debug("iniファイルにTrainingFlg=0が設定されているため動作モードを訓練に変更")
              # 動作モードを"訓練"に変更
              @mode = 1
            end
          end
        end
        
        # contentsがファイルの配列のため
        # 全てのファイルに対してuploadを行う
        contents.each do |entry|
          attribute = {}
          file = entry['file']
          name = entry['name']
          token = Redmine.post_uploads(file)
          unless token.blank?
            attribute['filename'] = name
            attribute['token'] = token
            attributes.push(attribute)
          end
        end
        
        if attributes.empty?
          @log.warning("Redmineへのファイルアップロードに成功しなかったため処理を中断します")
          raise RuntimeError.new('Redmineへのファイルアップロードに成功しませんでした。')
        end
        
        # 送信電文作成
        issue_json = create_issue_json(attributes)
        
        # Redmineへ送信
        Redmine::post_issues(issue_json)
  
      end
      
private
  
      #
      # issue登録用のJSONデータを作成する
      #
      def create_issue_json(attributes)
        
        # 設定取得
        config = Mrsss::get_redmine_config['txt_config']
        
        # issueに登録するためのJSONデータ用Hash
        json = {}
        issue = {}
        json['issue'] = issue
        
        # プロジェクトID
        if @mode == 1
          issue['project_id'] = Mrsss::get_mrsss_config['trainingmode_project_id']
        else
          issue['project_id'] = config['project_id']
        end
        # トラッカーID
        issue['tracker_id'] = config['tracker_id']
        # 題名
        issue['subject'] = config['subject']
        
        uploads = []
        issue['uploads'] = uploads
        attributes.each do |attribute|
          upload = {}
          upload['token'] = attribute['token']
          upload['filename'] = attribute['filename']
          upload['description'] = config['description']
          upload['content_type'] = config['content_type']
          uploads.push(upload)
        end
        
        log_str = "[#{@channel_id}] 送信JSONデータ\n"
        log_str = "#{log_str}--------------------------------------------------------------------------------\n"
        log_str = "#{log_str}#{json}\n"
        log_str = "#{log_str}--------------------------------------------------------------------------------"
        @log.debug(log_str)
  
        json.to_json
        
      end  
  
    end #Txt
  end # Parsers
end # Mrsss
