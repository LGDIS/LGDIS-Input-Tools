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

import jp.lg.ishinomaki.city.mrs.rest.PostController;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

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
            log.severe("XML解析に失敗したため処理中断");
            return;
        }

        // for debug 
        System.out.println(xml);
        
        // --------------------------------------------------------------
        // xmlを解析しxml内容をpostのxmlデータに紐付け
        // この処理を定義体を用いて汎用的に行う
        // TODO 設計待ち
        // --------------------------------------------------------------
        // xml解析（現時点では固定クラスで行う）
        // JAlertKishouDataParser parser = new JAlertKishouDataParser();
        // boolean isSuccessParse = parser.perse(xml);
        // if (isSuccessParse == false) {
        // log.severe("XML解析に失敗したため処理中断");
        // return;
        // }

        // for test
        // テスト用の送信データ作成
        String sendData = createTestXml();

        // rest送信
        PostController postController = new PostController();
        postController.post(sendData);
    }

    // for test
    // テスト用の送信XMLデータ作成メソッド
    // 本来であれば受信xmlを解析して送信xmlデータを作成する
    private String createTestXml() {

        Document doc = DocumentHelper.createDocument();

        Element issue = doc.addElement("issue");

        Element subject = issue.addElement("subject");
        subject.addText("データ受信機能テスト");

        Element project_id = issue.addElement("project_id");
        project_id.addText("rms-project"); // 外部入力用プロジェクト(固定)
        
        Element tracker_id = issue.addElement("tracker_id");
        tracker_id.addText("3");    // JMA(気象等環境変化)

        Element priority_id = issue.addElement("priority_id");
        priority_id.addText("2");   // 優先度 2 -> 通常

        return doc.asXML();
    }

}
