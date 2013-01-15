//
//  DataAnalyzer.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.analyzer;

/**
 * JMAソケット通信で受信した電文の解析を行うためのインターフェースです。<br>
 * 実際の解析処理は電文の内容により異なるためこのインターフェースを実装したクラスで行います。
 *
 */
public interface DataAnalyzer {
    /**
     * 引数で与えられた電文の内容を解析して内容を自インスタンスに保持します。<br>
     * 解析後のデータは各getterメソッドで取得してください。
     * 
     * @param data JMA通信で取得したユーザデータ部のbyte配列
     */
    public void analyze(byte[] data);
    
    /**
     * BCHヘッダー部を取得します。
     * 
     * @return BCH BCHインスタンス
     */
    public BCH getBCH();
    
    /**
     * 冒頭符部分を取得します。<br>
     * 冒頭符が存在しない場合はnullを返却します。
     * 
     * @return String 冒頭符
     */
    public String getHeaderCode();
    
    /**
     * 発信官署名を取得します。<br>
     * 発信官署名が存在しない場合はnullを返却します。
     * 
     * @return String 発信官署名
     */
    public String getSenderSign();
    
    /**
     * 観測日時刻を取得します。<br>
     * 観測日時刻が存在しない場合はnullを返却します。
     * 
     * @return String 観測日時刻
     */
    public String getObservationDate();
    
    /**
     * 指定コードを取得します。<br>
     * 指定コードが存在しない場合はnullを返却します。
     * 
     * @return String 指定コード
     */
    public String getAppointCode();
    
    /**
     * 本文を取得します。<br>
     * 存在しない場合はnullを返却します。
     * 
     * @return
     */
    public byte[] getContents();
    
    
}
