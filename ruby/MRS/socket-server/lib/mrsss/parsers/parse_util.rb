# coding: UTF-8

module Mrsss
  module Parsers
    
    #
    # XML解析時に使用するユーティリティメソッドを定義します。
    #
    module ParseUtil
    
      #
      # point情報をRest用文字列に変換します。緯度と経度の情報のみ使用し、深さの値は捨てます。ex.<tt>+21.2+135.5/ -> (135.5,21.2)</tt> 
      # 
      # ==== Args
      # _str_ :: 緯度経度情報 <tt>+21.2+135.5/</tt>
      # ==== Return
      # _String_ :: 緯度経度情報 <tt>(135.5,21.2)</tt>
      # ==== Raise
      def self.convert_point(str)
        # 正規表現を利用して符号、小数点、数値のみ取得
        m = str.scan(/([¥+¥-][0-9.]+)/)
        lat = m[0][0]
        lng = m[1][0]
        
        # 緯度情報から度分秒形式か判定
        if is_japanese_datum(lat)
          # 緯度、経度をそれぞれ度分秒形式から度形式に変換
          lat = to_world_datum(lat)
          lng = to_world_datum(lng)
        end
        return "(#{lng},#{lat})"
      end
  
      #
      # point情報の配列表現をRest用文字列に変換します。 ex.<tt>+35+135/+36+136/ -> ((+135,+35),(+136,+36))</tt>
      #
      # ==== Args
      # _str_ :: 緯度経度情報の配列表現 <tt>+35+135/+36+136/</tt>
      # ==== Return
      # _String_ :: 緯度経度情報の配列表現 <tt>((+135,+35),(+136,+36))</tt>
      # ==== Raise
      def self.convert_point_array(str)
        
        # 引数をスラッシュ(/)区切りで分割
        point_array = str.split('/', 0)
        # 要素が1つもない場合は処理終了
        if point_array.length == 0
          return []
        end
        
        # 全てのポイント情報
        converted_point_array = []
        point_array.each do |point|
          converted_point_array.push(convert_point(point))
        end
        
        # point_arrayの内容をカンマ区切りで1つの文字列に連携
        ret_str = ''
        converted_point_array.each do |point|
          ret_str = "#{ret_str}#{point}"
        end
        
        return "(#{ret_str})"
      end
  
      #
      # 引数の文字列(緯度)が日本測地系かを判定します。
      # 
      # ==== Args
      # _str_ :: 緯度経度情報
      # ==== Return
      # _bool_ :: true:度分秒形式(日本測地系) false:世界測地系
      # ==== Raise
      def self.is_japanese_datum(str)
        # まずは先頭の符号を除去
        if str.start_with?('+') || str.start_with?('-')
          str = str.slice(1, str.length)
        end
        
        # 文字列から整数部を取得するためピリオドの文字位置を検索
        period_index = str.index('.')
        
        # ピリオドの前までの部分文字列(整数部)を取得
        unless period_index.blank?
          str = str.slice(0, period_index)
        end
        
        # 整数部の桁数より度分秒形式を判定
        if str.length >= 3
          # 度分秒形式
          return true
        else
          # 度形式
          return false
        end
      end
  
      #
      # 日本測地系を世界測地系の値に変換します。
      #
      # ==== Args
      # _str_ :: 日本測地系による緯度経度情報の文字列表現
      # ==== Return
      # _String_ :: 世界測地系による緯度経度情報の文字列表現
      # ==== Raise
      def self.to_world_datum(str)
        
        # 符号と数値部を分割
        sign = ''
        if str.start_with?('+') || str.start_with?('-')
          sign = str.slice(0,1)
          str = str.slice(1, str.length - 1)
        end
        
        # 引数文字列を度、分、秒に分割
        str_do = ''
        str_fun = ''
        str_byo = ''
        period_index = str.index('.')
        if period_index.blank?
          str_do = str.slice(0, str.length - 2)
          str_fun = str.slice(str.length - 2, 2)
          str_byo = '00'
        else
          str_do = str.slice(0, period_index-2)
          str_fun = str.slice(period_index-2, 2)
          str_byo = str.slice(period_index+1, str.length)
        end
        
        # 度、分、秒形式を度形式に変換して文字列化
        float_do = str_do.to_f
        float_fun = str_fun.to_f / 60
        float_byo = str_byo.to_f / 3600
        float_degree = float_do + float_fun + float_byo
        str_degree = float_degree.to_s
        
        # 小数点2桁目で文字列を切り、符号を付与してリターン
        period_index2 = str_degree.index('.')
        if period_index2.blank?
          return sign + str_degree
        elsif period_index2 + 3 > str_degree.length
          return sign + str_degree
        else 
          return sign + str_degree.slice(0, period_index2 + 3)
        end
      end
      
      #
      # 震度を表す文字列を数値にして返却します。 ex1. "5+" -> 5.75 ex.2 "5-" -> 5.25
      #
      # ==== Args
      # _str_ :: 震度を表す文字列
      # ==== Return
      # _Integer_ :: 数値化した震度
      # ==== Raise
      def self.intensity_to_f(str)
        ret = 0.0
        if str.length == 1
          ret = str.to_f
        else 
          if str.slice(1,1) == '+'
            ret = str.slice(0,1).to_f + 0.75
          elsif str.slice(1,2) == '-'
            ret = str.slice(0,1).to_f + 0.25
          end
        end
        ret
      end

      # ==== Args
      # _str_ :: 震度を表す数値文字列
      # ==== Return
      # _String_ :: 震度
      # ==== Raise
      def self.add_modifier(str)
        log = Mrsss.parser_logger
        # str がnilならそのまま
        return str unless str
        return "震度" + str if str.size == 1
        return "震度" + str[0,1] + "強" if str.size == 2 && str[1,1] == "+" 
        return "震度" + str[0,1] + "弱" if str.size == 2 && str[1,1] == "-" 
      end
    
    end # ParseUtil
  end # Parsers
end # Mrsss
