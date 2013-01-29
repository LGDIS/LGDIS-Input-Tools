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
     * 本文を取得します。<br>
     * 存在しない場合はnullを返却します。
     * 
     * @return
     */
    public byte[] getContents();
    
    /**
     * データ種別を取得します。<br>
     * データ種別は3バイト文字列でキューに登録するデータのヘッダとして使用されます。<br>
     * 使用される3バイト文字列は<code>Consts</code>クラスに定義しています。
     * 
     * @return
     */
    public String getDataType();
}
