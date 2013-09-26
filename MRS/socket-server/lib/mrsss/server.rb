# coding: UTF-8

module Mrsss

  #
  # JMAソケット手順に従ってデータを受信するサーバクラスです。1ポートに対して当クラスのインスタンスを1つ割り当ててください。
  #
  class Server
    
    # クライアントから受信するデータの最大サイズ
    MAX_BUFFER = 720010

    # JMAソケットヘッダー長
    HEADER_SIZE = 10
    
    # ヘッダー内電文長部分の開始インデックス
    HEADER_LENGTH_OFFSET = 0
    # ヘッダー内電文長部分のサイズ
    HEADER_LENGTH_SIZE = 8

    # ヘッダー内メッセージ種別部分の開始インデックス
    HEADER_MSGTYPE_OFFSET = 8
    # ヘッダー内メッセージ種別部分のサイズ
    HEADER_MSGTYPE_SIZE = 2
    # 重複チェックデータ保持最大サイズ
    MAX_DUP_SIZE = 5

    #
    # 初期化処理です。
    #
    # ==== Args
    # _channel_name_ :: データの入力元を表す名称
    # _channel_id_ :: データの入力元を表す識別子
    # _port_ :: ポート番号
    # _archive_path_ :: アーカイブ先ディレクトリ
    # _mode_ :: 動作モード(0:通常 1:訓練 2:試験)
    # _need_checksum_ :: チェックサムの実施有無(true:チェックサム実施 false:チェックサム実施なし)
    # _use_queue_ :: Resqueue使用有無
    # ==== Return
    # ==== Raise
    def initialize(channel_name, channel_id, port, archive_path, mode, need_checksum, use_queue)
      
      raise ArgumentError.new("パラメータエラー。設定ファイルを確認してください。") if channel_id.blank? || port.blank? || archive_path.blank?
      
      @channel_name = channel_name
      @channel_id = channel_id
      @port = port
      @archive_path = archive_path
      @mode = mode
      @need_checksum = need_checksum
      @use_queue = use_queue
      @user_data_length_list = Array.new
      @joined_message = Array.new
      @log = Mrsss.server_logger
      @addr_info = Array.new
      @total_message_length = Array.new
      @user_data_length_list = Array.new
      @locker = Mutex::new
    end
    
    #
    # サーバの受信待ち処理を開始します。サーバソケットを許可モードでオープンしクライアントからの接続を待ちます。
    #
    # ==== Args
    # ==== Return
    # ==== Raise
    def start
      
      begin
        str_log = "[#{@channel_id}] JMA受信サーバ起動\n"
        str_log = "#{str_log}--------------------------------------------------------------------------------\n"
        str_log = "#{str_log}* チャネル名           [#{@channel_name}]\n"
        str_log = "#{str_log}* チャネル識別子       [#{@channel_id}]\n"
        str_log = "#{str_log}* 受信ポート           [#{@port}]\n"
        str_log = "#{str_log}* アーカイブパス       [#{@archive_path}]\n"
        str_log = "#{str_log}* 通常/訓練/試験モード [#{@mode}]\n"
        str_log = "#{str_log}* チェックサム実施有無 [#{@need_checksum}]\n"
        str_log = "#{str_log}* キュー使用有無       [#{@use_queue}]\n"
        str_log = "#{str_log}--------------------------------------------------------------------------------"
        @log.info(str_log)
        
        # TCPServerインスタンス生成
        @server = @server || TCPServer.open(@port)
        
        # Runs event loop.
        while true
          # to avoid heavy load on CPU
          sleep(0.1)
          @log.info("[#{@channel_name}] 接続待ち...")
          
          # クライアントからの接続待ち
          # このタイミングでスレッドブロックとなる
          session = @server.accept
          
          @log.info("[#{@channel_name}] 接続しました")
          @log.info("[#{@channel_name}] 接続先情報：#{session.peeraddr}")
          
          # 新規にスレッド実行
          Thread.new(session) { |c|
            handle_request(c)
          }
          
        end
        
      rescue => error
        @log.fatal("[#{@channel_name}] ソケット接続待ちで例外が発生しました。");
        @log.fatal(error)
      ensure
        # サーバクローズ
        unless @server.nil?
          @server.close
        end
      end
      
      @log.info("[#{@channel_name}] JMA受信サーバを停止します")
      @joined_message = Array.new
      @user_data_length_list = Array.new
    end
    
    #
    # 接続が確立したソケットに対してデータ受信処理を行います。
    #
    # ==== Args
    # _session_ :: クライアントと接続が確立したTCPSocketインスタンス
    # ==== Return
    # ==== Raise
    def handle_request(session)
      
      begin
        # Runs event loop.
        while true
          
          # to avoid heavy load on CPU
          sleep(0.1)
          
          @log.info("[#{@channel_name}] データ受信待ち...")
          
          data = session.recv(MAX_BUFFER)
          addr = session.peeraddr
          
          # 入力がなくなれば処理終了
          if data.empty?
            @log.info("[#{@channel_name}] 空データのためデータ受信を終了します")
            break
          end
          
          @locker.synchronize do
            @log.info("[#{@channel_name}] データを受信しました データ長[#{data.length}]")
            @log.info("[#{@channel_name}] データ送信先情報：#{addr}")

            #クライアント側はポート番号をランダムに割り当てるため、ホスト名に限定
            #addrstring = addr.join
            addrstring = addr[2]
            unless @addr_info.size > 0 && @addr_info.include?(addrstring)
              @addr_info.push(addrstring)
              @joined_message.push("")
              @total_message_length.push(0)
              @user_data_length_list.push([])
            end

            # データ解析処理
            handle_data(data, session, @addr_info.index(addrstring))
          end
        end
      rescue => error
        @log.fatal("[#{@channel_name}] データ受信待ちで例外が発生しました。");
        @log.fatal(error)
      ensure
        unless session.nil?
          @addr_info.delete(addrstring)
          @joined_message.delete_at(@addr_info.index(addrstring))
          @total_message_length.delete_at(@addr_info.index(addrstring))
          @user_data_length_list.delete_at(@addr_info.index(addrstring))
          session.close
        end
      end
      
      @log.info("[#{@channel_name}] データ受信待ちを停止します")
      #@joined_message = nil
      #@user_data_length_list = Array.new
    end
    
    #
    # 受信したデータを解析します。
    #
    # ==== Args
    # _data_ :: ソケットから受信したデータ
    # _session_ :: クライアントと接続が確立したTCPSocketインスタンス
    # _addr_id_ :: TCPSocketインスタンスの送信元index
    # ==== Return
    # ==== Raise
    def handle_data(data, session, addr_id)
      
      str_log = ""
      # 分割データではない場合は受信したデータで新規にMessageインスタンスを作成
      # 分割データの場合は保存されているMessageインスタンスにデータを結合
      if @joined_message[addr_id].blank?
        @log.debug("[#{@channel_name}] 新規の受信データです addr_id=#{addr_id}")

        # JMAソケットヘッダ部の解析
        header = data[0, HEADER_SIZE]
        message_type = header[HEADER_MSGTYPE_OFFSET, HEADER_MSGTYPE_SIZE].to_s
        @total_message_length[addr_id] = HEADER_SIZE + header[HEADER_LENGTH_OFFSET, HEADER_LENGTH_SIZE].to_i
        @actual_data_length = data.length
        @user_data_length_list[addr_id] << header[HEADER_LENGTH_OFFSET, HEADER_LENGTH_SIZE].to_i
        @joined_message[addr_id] = data
        str_log = "[#{@channel_name}] 解析データ\n"
        str_log = "#{str_log}--------------------------------------------------------------------------------\n"
        str_log = "#{str_log}* データ長    [#{@user_data_length_list[addr_id][0]}]\n"
        str_log = "#{str_log}* メッセージ種別  [#{message_type}]\n"
        str_log = "#{str_log}--------------------------------------------------------------------------------"
        @log.info(str_log)
      else
        @log.debug("[#{@channel_name}] 分割の受信データです addr_id=#{addr_id}")
        @joined_message[addr_id] = @joined_message[addr_id] + data
        @actual_data_length = @joined_message[addr_id].length
      end
      

      # 分割データを判定
      completed = false
      # 理論上の長さと実メッセージの長さの差分を元に分割データの処理を分ける
      diff = @actual_data_length - @total_message_length[addr_id]
      if diff > 0 && diff < HEADER_SIZE
        #差分有り：まとめ送りで次のヘッダ情報が取得不能のため、後続のデータを取得する必要有り。
        @log.debug("[#{@channel_name}] 実サイズ#{@actual_data_length} > 理論サイズ(#{@total_message_length[addr_id]}):HEADER_SIZE以内")
        completed = false
      elsif diff >= HEADER_SIZE
        #差分有り：まとめ送りのため、後続のヘッダから情報を取得する必要有り。
        @log.debug("[#{@channel_name}] 実サイズ#{@actual_data_length} > 理論サイズ")
        while @actual_data_length > @total_message_length[addr_id]
          next_message_size = @joined_message[addr_id][@total_message_length[addr_id] + HEADER_LENGTH_OFFSET, HEADER_LENGTH_SIZE].to_i
          next_message_type = @joined_message[addr_id][@total_message_length[addr_id] + HEADER_MSGTYPE_OFFSET, HEADER_MSGTYPE_SIZE]

          str_log = "[#{@channel_name}] 解析データ(まとめ送り処理中)\n"
          str_log = "#{str_log}--------------------------------------------------------------------------------\n"
          str_log = "#{str_log}* データ長    [#{next_message_size}]\n"
          str_log = "#{str_log}* メッセージ種別  [#{next_message_type}]\n"
          str_log = "#{str_log}--------------------------------------------------------------------------------"
          @log.info(str_log)

          # ここで想定の位置にメッセージ種別がない場合は不正なデータと判断
          if next_message_type != "aN" && next_message_type != "AN" && \
                 next_message_type != "bI" && next_message_type != "BI" && \
                 next_message_type != "fX" && next_message_type != "FX" && \
                 next_message_type != "EN"
            raise RuntimeError.new("受信データの処理が正常に行えませんでした。")
          end

          @user_data_length_list[addr_id] << next_message_size
          @total_message_length[addr_id] = @total_message_length[addr_id] + HEADER_SIZE + next_message_size
        end

        if @actual_data_length == @total_message_length[addr_id]
          completed = true
        else
          completed = false
        end
      elsif diff < 0
        @log.debug("[#{@channel_name}] 実サイズ(#{@actual_data_length} < 理論サイズ(#{@total_message_length[addr_id]})")
        completed = false
      else
        @log.debug("[#{@channel_name}] 実サイズ == 理論サイズ")
        completed = true
      end

      # データが完全の場合、データの抽出処理を行う
      if completed
        # データ分割ではない場合
        @log.debug("[#{@channel_name}] データが揃ったため後続処理を行います")

        pointer = 0 # データ抽出のため、現在の位置を保存
        for user_data_length in @user_data_length_list[addr_id] do
          # データの抽出
          segment = @joined_message[addr_id][pointer, HEADER_SIZE + user_data_length]

          # 次のデータの位置を取得
          pointer = pointer + HEADER_SIZE + user_data_length

          message = Message.new(segment)

          # ヘルスチェック要求の場合はヘルスチェック応答を返却
          if message.healthcheck?
            @log.info("[#{@channel_name}] ヘルスチェックのため応答")
            session.write(Message.HELTHCHK_RESPONSE)
            session.flush
          else
            @log.info("[#{@channel_name}] ユーザデータのため解析")
            handler = Handler.new(@channel_name, @channel_id, @archive_path, @mode, @need_checksum, @use_queue)
            handler.handle(message)
          end # end message.healthcheck?
  
          # チェックポイント要求の場合はチェックポイント応答を返却
          if message.checkpoint?
            @log.info("[#{@channel_name}] チェックポイントのため応答")
            session.write(message.checkpoint_response)
            session.flush
          end # end message.checkpoint?
  
          str_log = "[#{@channel_name}] 受信データ\n"
          str_log = "#{str_log}--------------------------------------------------------------------------------\n"
          str_log = "#{str_log}* メッセージ長    [#{message.userdata_length}]\n"
          str_log = "#{str_log}* メッセージ種別  [#{message.message_type}]\n"
          str_log = "#{str_log}--------------------------------------------------------------------------------"
          @log.info(str_log)
        end # end for

        # 処理用メッセージと配列を初期化
        @joined_message[addr_id] = nil
        @user_data_length_list[addr_id] = []
      else
        # データ分割の場合
        @log.debug("[#{@channel_name}] 分割受信のため再度データ受信待ち状態になります")
        return  nil 
      end
    end
  end # Server
end # Mrsss
