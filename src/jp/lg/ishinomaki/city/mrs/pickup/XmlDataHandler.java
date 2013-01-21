//
//  XmlMessageHandler.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.pickup;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.parser.JAlertKishouDataParser;
import jp.lg.ishinomaki.city.mrs.rest.PostController;

/**
 * J-Alertから受信した本文データを取り扱います。<br>
 * このクラスではXMLの解析とRESTサーバへの送信処理を行います。
 * 
 */
public class XmlDataHandler implements PickupDataHandler {

    /**
     * ログ用
     */
    private final Logger log = Logger.getLogger(XmlDataHandler.class
            .getSimpleName());

    /**
     * JMAソケット通信で取得した本文データに対する処理を行います。
     * 
     * @param msg
     *            本文データ
     */
    @Override
    public void handle(byte[] data) {

        // 本文データをxml化
        String xml = null;
        try {
            xml = new String(data, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.severe("データをXML文字列に変換する際に文字エンコーディングの問題が発生しました。処理中断を中断します。");
            return;
        }

        // xmlデータを解析
        JAlertKishouDataParser parser = new JAlertKishouDataParser();
        boolean isSuccess = parser.parse(xml);
        if (isSuccess == false) {
            log.severe("XMLの解析に失敗したため処理を中断します。");
            return;
        }

        // 送信データを作成
        String sendData = parser.createIssuesXmlAsString();

        // for debug
        System.err.println(sendData);
        
        // rest送信
        PostController postController = new PostController();
        postController.post(sendData);
    }

}
