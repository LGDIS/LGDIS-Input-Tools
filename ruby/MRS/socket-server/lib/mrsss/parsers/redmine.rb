# coding: utf-8

module Mrsss
  module Parsers
    
    #
    # RedmineへRest要求する際に使用するモジュールです。
    #
    module Redmine
      
      #
      # Redmineへissues発行依頼を行います。
      #
      # ==== Args
      # _data_ :: 要求データ(JSON形式)
      # ==== Return
      # _String_ :: Redmineから返却されたissueデータ(JSON形式)
      # ==== Raise
      def self.post_issues(data)
        # ロガー取得
        log = Mrsss.parser_logger
        # Redmineへissuesを発行するための各種設定
        config = Mrsss::get_redmine_config()['issues']
        
        # urlの作成
        url = create_url(config)
        
        str_log = "Rest送信データ\n"
        str_log = "#{str_log}--------------------------------------------------------------------------------\n"
        str_log = "#{str_log}* url : #{url}\n"
        str_log = "#{str_log}* timeout : #{config['timeout']}\n"
        str_log = "#{str_log}* open_timeout : #{config['open_timeout']}\n"
        str_log = "#{str_log}--------------------------------------------------------------------------------"
        log.info(str_log)
        
        # post送信
        begin
        log.info(data)
          response = RestClient.post url, data, :content_type => :json, :timeout => config['timeout'], :open_timeout => config['open_timeout']
        rescue => e
          log.error("Redmineへのissue登録時にエラーが発生しました。")
          raise e
        end
        
        if response.code == 200 || response.code == 201
          log.info("Rest送信が成功しました")
        else
          log.info("Rest送信に失敗しました")
          log.info(response)
          raise RuntimeError.new("Redmineへのissue登録要求でエラーが返却されました。")
        end
        return response
      end
      
      #
      # Redmineへupload依頼を行います。
      #
      # ==== Args
      # _data_ :: 要求データ(ファイルデータ)
      # ==== Return
      # _String_ :: Redmineから返却されたtoken
      # ==== Raise
      def self.post_uploads(data)
        # ロガー取得
        log = Mrsss.parser_logger
        
        # Redmineへuploadsを発行するための各種設定
        config = Mrsss::get_redmine_config()['uploads']
        
        # urlの作成
        url = create_url(config)
        
        str_log = "Rest送信データ\n"
        str_log = "#{str_log}--------------------------------------------------------------------------------\n"
        str_log = "#{str_log}* url : #{url}\n"
        str_log = "#{str_log}* timeout : #{config['timeout']}\n"
        str_log = "#{str_log}* open_timeout : #{config['open_timeout']}\n"
        str_log = "#{str_log}--------------------------------------------------------------------------------"
        log.info(str_log)
        
        begin
          response = RestClient.post url, data, :content_type => "application/octet-stream", :timeout => config['timeout'], :open_timeout => config['open_timeout']
        rescue => e
          log.error("Redmineへのupload発行時にエラーが発生しました。")
          raise e
        end

        # responseからトークンを取得
        if response.code == 201
          log.info("Rest送信が成功しました")
          json_response = JSON.parse(response.body)
          return json_response["upload"]["token"]
        else
          log.error("Rest送信が失敗しました")
          log.error(response)
          raise RuntimeError.new("Redmineへのupload発行時にエラーが発生しました。")
        end
      end
      
private

      #
      # RestAPI用のURLを作成する
      #
      def self.create_url(config)
        # urlの作成
        basic_user = config['basic_user']
        basic_password = config['basic_password']
        url = ''
        if basic_user.blank? || basic_password.blank?
          url = "#{config['protocol']}://#{config['site']}/#{config['prefix']}&key=#{config['api_key']}"
        else
          url = "#{config['protocol']}://#{basic_user}:#{basic_password}@#{config['site']}/#{config['prefix']}&key=#{config['api_key']}"
        end
        url
      end
      
    end # Redmine
  end # Parsers
end # Mrsss
