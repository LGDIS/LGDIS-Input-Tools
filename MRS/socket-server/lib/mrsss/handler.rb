# coding: UTF-8

module Mrsss

  # 
  # 外部入力先より受信したデータを処理します。
  #
  class Handler

    cattr_accessor(:duplicate_contents)
    @@duplicate_contents = []
    MAX_DUP_SIZE = 10

    #
    # 初期化処理です。
    #
    # ==== Args
    # _channel_id_ :: データの入力元を表す識別子
    # _archive_path_ :: アーカイブ先ディレクトリ
    # _mode_ :: 動作モード(0:通常 1:訓練 2:試験)
    # _need_checksum_ :: チェックサムの実施有無(true:チェックサム実施 false:チェックサム実施なし)
    # _use_queue_ :: Resque使用有無(true:Resque使用 false:使用なし)
    # ==== Return
    # ==== Raise
    def initialize(channel_name, channel_id, archive_path, mode, need_checksum, use_queue)
      @channel_name = channel_name
      @channel_id = channel_id
      @archive_path = archive_path
      @mode = mode
      @need_checksum = need_checksum
      @use_queue = use_queue
      @log = Mrsss.server_logger
      @saved_message = nil
    end
    
    #
    # 受信データ処理を行います。データをヘッダ部とボディ部に分割しヘッダ部を解析します。
    #
    # ====Args
    # _message_ :: 受信データ
    # ==== Return
    # ==== Raise
    def handle(message)
      
      # キューに登録するデータ本文
      contents = nil
      
      # BCHの有の電文の場合はBCHを解析して本文部分を抽出
      if message.exist_bch?
        @log.debug("[#{@channel_name}] BCHが存在する電文のためBCH解析実施")
        # BCHとTCH解析
        message.analyze_bch
        
        # BCH解析後の本文データを取得
        contents = message.contents
        
        str_log = "[#{@channel_name}] BCH解析結果\n"
        str_log = "#{str_log}--------------------------------------------------------------------------------\n"
        str_log = "#{str_log}* バージョン      [#{message.bch_version}]\n"
        str_log = "#{str_log}* ヘッダ長        [#{message.bch_length}]\n"
        str_log = "#{str_log}* XML種別         [#{message.bch_xml_type}]\n"
        str_log = "#{str_log}* データ属性      [#{message.bch_data_attr}]\n"
        str_log = "#{str_log}* データ種別      [#{message.bch_data_type}]\n"
        str_log = "#{str_log}* A/N桁数         [#{message.bch_anlength}]\n"
        str_log = "#{str_log}* チェックサム    [#{message.bch_checksum}]\n"
        str_log = "#{str_log}--------------------------------------------------------------------------------"
        @log.info(str_log)
        
        # BCHバージョン1以外はチェックサム実施
        if message.bch_version != 1
          # 設定ファイルでチェックサム実施フラグがONの場合はチェックサム実施
          if @need_checksum == true
            # チェックサム実施
            unless message.checksum
              @log.error("[#{@channel_name}] チェックサムチェクエラーのため処理を中断します")
              return nil
            else
              @log.info("[#{@channel_name}] チェックサムチェックOKです")
            end
          else
            @log.info("[#{@channel_name}] 設定によりチェックサムチェックをスキップします")
          end
        end
        
        # 本文部分がgzip or zip圧縮されている場合は解凍する
        # tarファイルなどそれ以外の場合はそのまま使用
        # gzip圧縮
        if message.bch_xml_type == 2
          @log.debug("[#{@channel_name}] gzip圧縮された電文のためgzip解凍")
          contents = Util.ungzip(contents)
        # zip圧縮
        elsif message.bch_xml_type == 3
          @log.debug("[#{@channel_name}] zip圧縮された電文のためzip解凍")
          contents = Util.unzip(contents)
        elsif message.gzip?
          @log.debug("[#{@channel_name}] gzip圧縮された電文のためgzip解凍(BCH3)")
          contents = Util.ungzip(contents)
          if !contents.valid_encoding?
            if contents.force_encoding("Shift_JIS").valid_encoding?

             # convert to UTF-8
             converter = Encoding::Converter.new("Shift_JIS","UTF-8")
             contents = converter.convert(contents)
             contents.sub!("encoding=\"Shift_JIS\"","encoding=\"UTF-8\"")
            end
          end
        end
      else
        @log.debug("[#{@channel_name}] BCHが存在しない電文のためBCH解析を行わない")
        # BCH無しの場合はJmaMessageのユーザデータ部全てが本文部
        contents = message.userdata
      end
      
      # 拡張子を作成してデータ内容をアーカイブにする
      extension = gen_extension(message)
      Util.archive_ext(contents, @archive_path, @channel_name, extension)
      
      # message_typeが'JL'の場合はtarファイルのためtarファイルを解凍する
      # このtarファイル内のテキストデータはShift_JIS文字コードのため
      # resque登録時に文字化けが発生する
      # そのためresque登録前にテキストファイル形式に変換しておく
      if message.message_type == 'JL'
        @log.debug("[#{@channel_name}] メッセージ種別が[JL]のためtarファイルを解凍する")
        contents = Util.untar(contents)
      end

      if duplicate_contents.size > 0 && duplicate_contents.include?(contents)
        @log.info("[#{@channel_name}] 冗長化によるメッセージ重複のためキューイング対象外とする\n#{contents}")
        return
      else
        if duplicate_contents.size >= MAX_DUP_SIZE
          duplicate_contents.shift
        end
        duplicate_contents.push(contents)
      end

      # ファイルフォーマットを作成
      fileformat = gen_fileformat(message)
      
      # 後続処理へデータを渡す
      relay(contents, fileformat)
      
    end
    
    #
    # 後続処理へ依頼
    #
    # ==== Args
    # _contents_ :: 受信データ内容
    # _fileformat_ :: ファイルフォーマット
    # ==== Return
    # ==== Raise
    def relay(contents, fileformat)

      #
      # use_queueがtrueの場合はRequeにenqueueする
      # use_queueがfalseの場合はParserを直接コールする
      #
      # resque使用
      if @use_queue
        str_log = "[#{@channel_name}] 受信データをキューへ登録\n"
        str_log = "#{str_log}--------------------------------------------------------------------------------\n"
        str_log = "#{str_log}* チャネル名 [#{@channel_name}]\n"
        str_log = "#{str_log}* 通常/訓練/試験モード [#{@mode}]\n"
        str_log = "#{str_log}* チャネルID [#{@channel_id}]\n"
        str_log = "#{str_log}* ファイル形式 [#{fileformat}]\n"
        str_log = "#{str_log}--------------------------------------------------------------------------------"
        @log.info(str_log)
        begin
          Resque.enqueue(Mrsss::Parsers::Parser, contents, @mode, @channel_id, fileformat)
        rescue => exception
          @log.error("受信データのキュー登録に失敗しました。")
          @log.error(exception) 
        end
      
      # resque使用しない
      else
        str_log = "[#{@channel_name}] 受信データを解析クラスへ渡す\n"
        str_log = "#{str_log}--------------------------------------------------------------------------------\n"
        str_log = "#{str_log}* チャネル名 [#{@channel_name}]\n"
        str_log = "#{str_log}* 通常/訓練/試験モード [#{@mode}]\n"
        str_log = "#{str_log}* チャネルID [#{@channel_id}]\n"
        str_log = "#{str_log}* ファイル形式 [#{fileformat}]\n"
        str_log = "#{str_log}--------------------------------------------------------------------------------"
        @log.info(str_log)
        begin
          Mrsss::Parsers::Parser.perform(contents, @mode, @channel_id, fileformat)
        rescue => exception
          @log.error("受信データの解析クラスに失敗しました。")
          @log.error(exception) 
        end
      end
    end
    
    #
    # ファイルフォーマットを作成
    #
    # ==== Args
    # _message_ :: 受信データ(Message)
    # ==== Return
    # _String_ :: 受信データに対応するファイルフォーマット識別子
    # ==== Raise
    def gen_fileformat(message)
      fileformat = ''
      if message.message_type == 'JL' || message.message_type == 'AN' || message.message_type == 'aN'
        fileformat = 'TXT'
      elsif message.bch_xml_type == 1 || message.bch_xml_type == 2 || message.bch_xml_type == 3
        # only for BCH version 4
        fileformat = 'XML'
      elsif message.message_type == 'BI' || message.message_type == 'bI'
        if message.gzip?
          fileformat = 'XML'
        else
          fileformat = 'BIN'
        end
      end
      fileformat
    end
    
    #
    # 拡張子を作成
    #
    # ==== Args
    # _message_ :: 受信データ(Message)
    # ==== Return
    # _String_ :: 受信データに対応するファイル拡張子
    # ==== Raise
    def gen_extension(message)
      extension = ''
      if message.message_type == 'JL'
        extension = 'tar'
      elsif message.bch_xml_type == 1 || message.bch_xml_type == 2 || message.bch_xml_type == 3
        extension = 'xml'
      elsif message.message_type == 'BI' || message.message_type == 'bI'
        if message.gzip?
          extension = 'xml'
        else
          extension = 'bin'
        end
      elsif message.message_type == 'AN' || message.message_type == 'aN'
        extension = 'txt'
      end
      extension
    end
    
  end # Handler

end # Mrsss
