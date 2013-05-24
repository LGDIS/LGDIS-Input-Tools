# coding: UTF-8

module Mrsss
  module Parsers
    
    #
    # JMAから受信したPNGファイルをRedmineへ登録するための処理を行うクラスです。
    #
    class Png
  
      #
      # 初期化処理を行います。
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
      # PNGファイルのRedmineへの送信処理を行います。
      #
      # ==== Args
      # _contents_ :: PNGファイルデータ
      # ==== Return
      # ==== Raise
      def handle(contents)
        
        # PNGデータのアップロード
        token = Redmine::post_uploads(contents)
        
        # トークンが取得できなかった場合は処理中断
        if token.blank?
          @log.error("[#{@channel_id}] Redmineへのファイルアップロードの結果tokenが返却されなかったため処理を中断します。")
          
          return
        end
        
        # 送信電文作成
        issue_json = create_issue_json(token)
        
        # Redmineへ送信
        Redmine::post_issues(issue_json)
        
      end
      
  private
  
      #
      # issue登録用のJSONデータを作成する
      #
      def create_issue_json(token)
        
        # 設定取得
        config = Mrsss::get_redmine_config['png_config']
        
        # issueに登録するためのJSONデータ用Hash
        json = {}
        issue = {}
        json['issue'] = issue
        
        # プロジェクトID
        issue['project_id'] = config['project_id']
        # トラッカーID
        issue['tracker_id'] = config['tracker_id']
        # 題名
        issue['subject'] = config['subject']
        
        upload = {}
        upload['token'] = token
        upload['filename'] = config['filename']
        upload['description'] = config['description']
        upload['content_type'] = config['content_type']
        uploads = []
        uploads[0] = upload
        issue['uploads'] = uploads
        
        log_str = "[#{@channel_id}] 送信JSONデータ\n"
        log_str = "#{log_str}--------------------------------------------------------------------------------\n"
        log_str = "#{log_str}#{json}\n"
        log_str = "#{log_str}--------------------------------------------------------------------------------"
        @log.debug(log_str)
        
        json.to_json
      end
  
    end #Png
  end # Parsers
end # Mrsss