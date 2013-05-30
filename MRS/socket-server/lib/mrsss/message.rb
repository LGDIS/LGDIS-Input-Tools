# coding: UTF-8

module Mrsss
  
  #
  # JMAソケット通信により受信したデータを解析/保持するクラスです。
  #
  class Message
    
    # メッセージ種別
    # 文字データ(チェックポイントなし)
    MSGTYPE_AN = 'AN'
    # バイナリデータ(チェックポイントなし)
    MSGTYPE_BI = 'BI'
    # FAX図データ(チェックポイントなし)
    MSGTYPE_FX = 'FX'
    # 内閣官房Tarデータ(チェックポイントなし)
    MSGTYPE_JL = 'JL'
    # 文字データ(チェックポイントあり)
    MSGTYPE_aN = 'aN'
    # バイナリデータ(チェックポイントあり)
    MSGTYPE_bI = 'bI'
    # FAX図データ(チェックポイントあり)
    MSGTYPE_fX = 'fX'
    # 制御データ
    MSGTYPE_EN = 'EN'
    
    # 制御レコード種別
    # チェックポイント通知
    CTLTYPE_ACK = 'ACK'
    # ヘルスチェック要求
    CTLTYPE_chk = 'chk'
    # ヘルスチェック応答
    CTLTYPE_CHK = 'CHK'
    
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
    
    # コントロール種別部分の開始インデックス
    CTLTYPE_OFFSET = 10
    # コントロール種別部分のサイズ
    CTLTYPE_SIZE = 3
    
    # チェックポイント応答電文のサイズ
    CHECKPOINT_RESPONSE_SIZE = 30
    
    
    # ヘルスチェック応答
    @@HELTHCHK_RESPONSE = '00000003ENCHK'
    
    # チェックポイント応答のヘッダ部
    @@CHECKPOINT_RESPONSE_HEADER = '00000033ENACK'
    
    # クラス変数へのアクセサ定義
    cattr_reader :HELTHCHK_RESPONSE, :CHECKPOINT_RESPONSE_HEADER
  
    # インスタンス変数のアクセサ定義
    attr_reader :data, :message_length, :message_type, :userdata_length, :control_type, :str_bch
    
    #
    # 初期化処理です。
    #
    # ==== Args
    # _data_ :: 受信したデータ(String)
    # ==== Return
    # ==== Raise
    def initialize(data)
      # 生データ保存
      @data = data
      
      # JMAソケットヘッダ部の解析
      header = data[0, HEADER_SIZE]
      
      # メッセージ長取得
      @message_length = header[HEADER_LENGTH_OFFSET, HEADER_LENGTH_SIZE].to_i
      
      # メッセージ種別取得
      @message_type = header[HEADER_MSGTYPE_OFFSET, HEADER_MSGTYPE_SIZE].to_s
      
      # ユーザデータ長取得
      @userdata_length = data.length - HEADER_SIZE
      
      # 制御データの場合はコントロール種別を取得
      if @message_type == MSGTYPE_EN
        @control_type = data[CTLTYPE_OFFSET, CTLTYPE_SIZE].to_s
      end
    end
    
    #
    # インスタンスが保持する受信データに引数データを結合します。
    #
    # ==== Args
    # _data_ :: 結合する受信データ
    # ==== Return
    # _Message_ :: 自身のインスタンス
    # ====Raise
    def append(data)
      if @data.blank?
        return
      end
      # データを最後尾に連結
      @data = @data + data
      # データ長を再計算
      @userdata_length = @data.length - HEADER_SIZE
      self
    end
    
    #
    # チェックポイント応答データを取得します。
    #
    # ==== Args
    # ==== Return
    # _String_ :: チェックポイント応答データ
    # ==== Raise
    def checkpoint_response
      @@CHECKPOINT_RESPONSE_HEADER + @data[0, CHECKPOINT_RESPONSE_SIZE]
    end
    
    #
    # ユーザデータ部を取得します。
    #
    # ==== Args
    # ==== Return
    # _String_ :: ユーザデータ
    # ==== Raise
    def userdata
      @data[10, (@data.length - HEADER_SIZE)]
    end
    
    #
    # JMAソケットのヘッダ部にあるデータレングスと実際の受信データ長の一致判定を行います。
    # 
    # ==== Args
    # ==== Return
    # _bool_ :: true:一致 false:不一致
    # ==== Raise
    def complete?
      @message_length == @userdata_length
    end
    
    #
    # ヘルスチェック電文を判定します。
    #
    # ==== Args
    # ==== Return
    # _bool_ :: true:ヘルスチェック false:ヘルスチェック以外
    # ==== Raise
    def healthcheck?
      @message_type == MSGTYPE_EN && @control_type == CTLTYPE_chk
    end
    
    #
    # チェックポイント電文を判定します。
    #
    # ==== Args
    # ==== Return
    # _bool_ :: true:チェックポイント電文 false:チェックポイント電文以外
    # ==== Raise
    def checkpoint?
      @message_type == MSGTYPE_aN || @message_type == MSGTYPE_bI || @message_type == MSGTYPE_fX
    end
    
    #
    # BCHの有無を判定を判定します。
    #
    # ==== Args
    # ==== Return
    # _bool_ :: true:BCH有り false:BCH無し
    # ==== Raise
    def exist_bch?
      # メッセージ種別により判定する'JL'の場合はBCHなし
      # 上記以外はBCHありと判断する
      !(@message_type == MSGTYPE_JL)
    end
    
    #
    # BCH部を解析します。
    #
    # ==== Args
    # ==== Return
    # _String_ :: BCH部分の文字列表現
    # ==== Raise
    def analyze_bch
      
      # ユーザデータを対象に処理
      data = userdata
      
      # まずは先頭1バイトのデータを取得
      # 先頭4ビットがBCHバージョン情報で後続4ビットがBCHレングス
      bch_head1 = data[0]
      bch_ver_len = to_bit_str(bch_head1)
      
      # BCHレングス取得
      bch_length = bch_ver_len[4, 8].to_i(2) * 4 # BCH仕様より4をかけBCHレングスとする
      
      # BCHビットの文字列表現をインスタンス変数に保存
      tmp_bch = data[0, bch_length]
      @str_bch = to_bit_str(tmp_bch)
      
    end
    
    #
    # BCHのバージョンNoを取得します。
    #
    # ==== Args
    # ==== Return
    # _Integer_ :: バージョンNo
    # ==== Raise
    def bch_version
      @str_bch[0, 4].to_i(2)
    end
    
    #
    # BCH長を取得します。
    #
    # ==== Args
    # ==== Return
    # _Integer_ :: BCH長
    # ==== Raise
    def bch_length
      @str_bch[4, 4].to_i(2) * 4
    end
    
    #
    # XMLタイプを取得します。
    #
    # ==== Args
    # ==== Return
    # _Integer_ :: XMLタイプ
    # ==== Raise
    def bch_xml_type
      @str_bch[36, 2].to_i(2)
    end
    
    #
    # データ属性
    #
    # ==== Args
    # ==== Return
    # _Integer_ :: データ属性
    # ==== Raise
    def bch_data_attr
      @str_bch[40, 4].to_i(2)
    end

    #
    # データ種別
    #
    # ==== Args
    # ==== Return
    # _Integer_ :: データ種別
    # ==== Raise
    def bch_data_type
      @str_bch[48, 8].to_i(2)
    end

    #
    # BCH内のA/N桁数
    #
    # ==== Args
    # ==== Return
    # _Integer_ :: A/N桁数
    # ==== Raise
    def bch_anlength
      # バイナリ電文の場合のみ設定されている
      @str_bch[72, 8].to_i(2)
    end

    #
    # データがgzipかどうか
    #
    # ==== Args
    # ==== Return
    # _bool_ :: true:gzip false:gzip以外
    # ==== Raise
    def gzip?
      if @message_type == MSGTYPE_BI || @message_type == MSGTYPE_bI
        if exist_bch?
          if to_bit_str(@data[HEADER_SIZE + bch_length + bch_anlength, 2]) == format("%016b",0x1f8b)
            return true
          end
        end
      end
      return false
    end
    
    #
    # BCH内のチェックサム値を取得します。
    #
    # ==== Args
    # ==== Return
    # _String_ :: チェックサム値
    # ==== Raise
    def bch_checksum
      @str_bch[80, 16]
    end
    
    #
    # TCH部を取得します。
    #
    # ==== Args
    # ==== Return
    # _String_ :: TCH部
    # ==== Raise
     def tch
      # BCH後からA/N桁数分がTCH部
      data = userdata
      data[bch_length, anlength]
    end
    
    #
    # 本文部を取得します。
    #
    # ==== Args
    # ==== Return
    # _String_ :: 本文部
    # ==== Raise
    def contents
      data = userdata

      # AN形式の電文本文は仕様を元に正規表現で判定する
      if @message_type == 'aN' || @message_type == 'AN'
        reg_exp_not_addressed = /\x0a.{0,12}\x20.{0,12}\x20.{6}\x20.{3,5}\x0a{1,}(?:\x02\x0a)?/
        reg_exp_addressed = /\x0a.{0,12}\x20.{0,12}\x20.{2}\x20.{3,5}\x0a.{0,2}\x0a.*\x0a\x02\x0a/
        if data =~ reg_exp_not_addressed
          return data[bch_length..-1].gsub(reg_exp_not_addressed,"")
        elsif data =~ reg_exp_addressed 
          return data[bch_length..-1].gsub(reg_exp_addressed,"")
        end
      end

      return data[bch_length + bch_anlength, (@userdata_length - bch_length - bch_anlength)]
    end
    
    #
    # チェックサムを実施します。
    #
    # ==== Args
    # ==== Return
    # _bool_ :: チェックサム結果 true:OK false:NG
    # ==== Raise
    def checksum
      sum = 0
      # BCHを16桁づつ分割し全て加算
      0.upto(9) {|index|
        # チェックサム部分は加算をスキップ
        next if index == 5
        tmp = @str_bch[index*16, 16]
        sum = sum + tmp.to_i(2)
      }
      
      # 加算結果を32桁の文字列に変換
      sum = format("%.32b", sum)
      
      # 上位16桁が0になるまで上位16桁を下位16桁に加算
      upper = 0
      under = 0
      loop do
        # 上位16桁を取得
        upper = sum[0, 16].to_i(2)
        under = sum[16,16].to_i(2)
        
        break if upper == 0
        
        # 上位16桁と下位16桁を加算
        tmp_sum = upper + under
        
        # 加算結果を32桁の文字列に変換
        sum = format("%.32b", sum)
      end
      
      # 計算されたチェックサム値
      calculated_checksum = format("%16b", ~under&0xFFFF)
      
      # BCH内のチェックサム値と計算されたチェックサム値を比較する
      bch_checksum == calculated_checksum
    end
    
    #
    # バイナリデータを２進数(bit)表現の文字列に変換します。1バイトを8桁のビット文字列とします。
    #
    # ==== Args
    # _data_ :: バイナリデータ(String)
    # ==== Return
    # _String_ :: 2進数表現の文字列 ex."0101100110100010"
    def to_bit_str(data)
      str = ''
      io = StringIO.new(data)
      io.each_byte { |ch|
        str = str + format("%.8b", ch)
      }
      str
    end

  end # Message

end # Mrsss
