//
//  XmlMessageHandler.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.pickup;

import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.parser.JmaXmlDataParser;
import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;
import jp.lg.ishinomaki.city.mrs.parser.XmlSchemaChecker;
import jp.lg.ishinomaki.city.mrs.rest.IssuesPostController;
import jp.lg.ishinomaki.city.mrs.utils.FileUtils;

import org.dom4j.CDATA;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

/**
 * J-Alertから受信した本文データを取り扱います。<br>
 * このクラスではXMLの解析とRESTサーバへの送信処理を行います。
 * 
 */
public class JmaXmlDataHandler implements PickupDataHandler {

    /**
     * ログ用
     */
    private final Logger log = Logger.getLogger(JmaXmlDataHandler.class
            .getSimpleName());

    /**
     * 動作モード 0:通常 1:訓練 2:試験
     */
    private int mode = 0;

    public JmaXmlDataHandler() {
        this(0);
    }

    /**
     * コンストラクタ.<br>
     * 引数で動作モードを指定
     * 
     * @param mode
     */
    public JmaXmlDataHandler(int mode) {
        this.mode = mode;
    }

    /**
     * JMAソケット通信で取得した本文データに対する処理を行います。
     * 
     * @param msg
     *            本文データ
     */
    @Override
    public void handle(byte[] data) {

        // データなしの場合は処理しない
        if (data == null || data.length == 0) {
            return;
        }
        
        // 本文データをxml化
        String xml = null;
        try {
            xml = new String(data, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.severe("データをXML文字列に変換する際に文字エンコーディングの問題が発生しました。処理中断を中断します。");
            return;
        }

        // xlmデータのスキーマチェックを実施
        // スキーマファイル名取得
        String schemaFilePath = ParserConfig.getInstance()
                .getJmaSchemaFilePath();
        boolean isValid = XmlSchemaChecker.getInstatnce(schemaFilePath)
                .validate(xml);
        if (isValid == false) {
            log.severe("XMLのスキーマチェックでNGだったため処理を中断します。");
            return;
        }

        // xmlデータを解析
        JmaXmlDataParser parser = new JmaXmlDataParser();
        boolean isSuccess = parser.parse(xml);
        if (isSuccess == false) {
            log.severe("XMLの解析に失敗したため処理を中断します。");
            return;
        }

        // 送信データを作成
        String sendData = createIssuesXmlAsString(parser);

        log.finest("------------------------Redmineへの送信データ------------------------\n"
                + sendData
                + "\n--------------------------------------------------------------------");

        // RedmineのRestApi(Post)実行
        IssuesPostController postController = new IssuesPostController();
        postController.post(sendData);
    }

    /**
     * Issuesに渡すxmlデータを作成します
     * 
     * @return
     */
    String createIssuesXmlAsString(JmaXmlDataParser parser) {

        Document doc = DocumentHelper.createDocument();

        // ルートは"issue"
        Element issue = doc.addElement("issue");

        // 動作モードが訓練or試験の場合は専用のプロジェクトIDを使用する
        if (mode == 1) {
            Element project_id = issue.addElement("project_id");
            project_id.addText(ParserConfig.getInstance()
                    .getTrainingProjectId());
        } else if (mode == 2) {
            Element project_id = issue.addElement("project_id");
            project_id.addText(ParserConfig.getInstance().getTestProjectId());
        } else {
            // プロジェクト自動立ち上げフラグがONの場合
            if (parser.isAutoLaunch()) {
                Element auto_launch = issue.addElement("auto_launch");
                auto_launch.addText("1");
            } else {
                // 固定のプロジェクトID設定
                // プロジェクト自動立ち上げの場合は設定しない
                Element project_id = issue.addElement("project_id");
                project_id.addText(parser.getProjectId());
            }
        }

        // プロジェクト自動配信フラグがONの場合
        if (parser.isAutoSend()) {
            Element auto_send = issue.addElement("auto_send");
            auto_send.addText("1");
        }

        // トラッカーID設定
        Element tracker_id = issue.addElement("tracker_id");
        tracker_id.addText(parser.getTrackerId());

        // control部
        Element xml_control_element = issue.addElement("xml_control");
        CDATA controlCDATA = DocumentHelper.createCDATA(parser.getXmlControl());
        xml_control_element.add(controlCDATA);

        // head部
        Element xml_head_element = issue.addElement("xml_head");
        CDATA headCDATA = DocumentHelper.createCDATA(parser.getXmlHead());
        xml_head_element.add(headCDATA);

        // body部
        Element xml_body_element = issue.addElement("xml_body");
        CDATA bodyCDATA = DocumentHelper.createCDATA(parser.getXmlBody());
        xml_body_element.add(bodyCDATA);

        // Issues拡張カラム用データ設定
        Map<String, String> issueExtraMap = parser.getIssueExtraMap();
        for (String key : issueExtraMap.keySet()) {
            Element element = issue.addElement(key);
            element.addText(issueExtraMap.get(key));
        }

        // issues_geographiesにデータを設定
        List<Map<String, String>> issueGeographyMaps = parser
                .getIssueGeographyMaps();
        if (issueGeographyMaps.size() > 0) {
            Element issueGeographies = issue.addElement("issue_geographies");
            issueGeographies.addAttribute("type", "array");
            for (Map<String, String> issueGeographyMap : issueGeographyMaps) {
                Element issueGeography = issueGeographies
                        .addElement("issue_geography");
                for (String key : issueGeographyMap.keySet()) {
                    Element e = issueGeography.addElement(key);
                    e.addText(issueGeographyMap.get(key));
                }
            }
        }

        // for test ------------------------------------
        String subject = issueExtraMap.get("subject");
        toXmlFile(doc, subject);
        // for test ------------------------------------

        return doc.asXML();
    }

    /**
     * テスト用メソッド.<br>
     * createIssuesXmlAsStringメソッドで作成したXMLをファイルに出力する
     */
    private void toXmlFile(Document doc, String subject) {
        XMLWriter xw = null;
        try {
            // ファイル名
            String fileName = FileUtils.genFileName(subject);
            xw = new XMLWriter(new FileWriter(fileName));
            xw.write(doc);
            xw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (xw != null) {
                try {
                    xw.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
