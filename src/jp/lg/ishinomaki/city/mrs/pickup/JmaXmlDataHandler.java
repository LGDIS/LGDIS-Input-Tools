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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.parser.JmaXmlDataParser;
import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;
import jp.lg.ishinomaki.city.mrs.parser.XmlSchemaChecker;
import jp.lg.ishinomaki.city.mrs.rest.IssuesPostController;
import jp.lg.ishinomaki.city.mrs.utils.DateUtils;
import jp.lg.ishinomaki.city.mrs.utils.FileUtils;
import jp.lg.ishinomaki.city.mrs.utils.StringUtils;

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

    /**
     * プロジェクト自動立ち上げフラグを送信した最終時刻を保存
     */
    static Date lastDateTimeAutoLaunch;

    /**
     * プロジェクト自動送信フラグを送信した最終時刻を保存
     */
    static Date lastDateTimeAutoSend;

    /**
     * コンストラクタ.
     * 
     */
    public JmaXmlDataHandler() {
        this(0);
    }

    /**
     * コンストラクタ.<br>
     * 引数で動作モードを指定
     * 
     * @param mode
     *            動作モード 0:通常 1:訓練 2:試験
     */
    public JmaXmlDataHandler(int mode) {
        this.mode = mode;
    }

    /**
     * JMAソケット通信で取得した本文データに対する処理を行います。
     * 
     * @param data
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
        // スキーマファイルの設定がある場合はスキーマチェック
        if (StringUtils.isBlank(schemaFilePath) == false) {
            boolean isValid = XmlSchemaChecker.getInstatnce(schemaFilePath)
                    .validate(xml);
            if (isValid == false) {
                log.severe("XMLのスキーマチェックでNGだったため処理を中断します。");
                return;
            }
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
        System.out
                .println("------------------------Redmineへの送信データ------------------------\n"
                        + sendData
                        + "\n--------------------------------------------------------------------");
        // RedmineのRestApi(Post)実行
        IssuesPostController postController = new IssuesPostController();
        postController.post(sendData);
    }

    /**
     * Issuesに渡すxmlデータを作成します
     * 
     * @param parser
     *            JmaXMLデータ解析インスタンス
     * @return String Redmine送信用のXML文字列を作成
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
            // 通常モードの場合はルールから取得したプロジェクトIDを設定
            Element project_id = issue.addElement("project_id");
            project_id.addText(parser.getProjectId());
        }

        // プロジェクト自動立ち上げフラグがONの場合
        if (parser.isAutoLaunch()) {
            // プロジェクト自動立ち上げ間隔のチェック
            if (isAutoLaunchByInterval(parser.getAutoLaunchInterval())) {
                Element auto_launch = issue.addElement("auto_launch");
                auto_launch.addText("1");
            }
        }

        // プロジェクト自動配信先の指定(○号配備)がある場合
        if (parser.getDisposition() != null) {
            // プロジェクト自動送信間隔のチェック
            if (isAutoSendByInterval(parser.getAutoSendInterval())) {
                Element auto_send = issue.addElement("auto_send");
                auto_send.addText(parser.getDisposition());
            }
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

        return doc.asXML();
    }

    /**
     * テスト用メソッド.<br>
     * createIssuesXmlAsStringメソッドで作成したXMLをファイルに出力する。<br>
     * 
     * @param doc
     *            Document全体
     * @param subject
     *            ファイル名の標題
     */
    @SuppressWarnings("unused")
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

    /**
     * 設定された時間間隔(分単位)によりプロジェクト自動立ち上げを行なってよいかを判定します。
     * 
     * @param rule
     *            解析ルールインスタンス
     * @return boolean true:自動立ち上げ許可 false:自動立ち上げ不可
     */
    boolean isAutoLaunchByInterval(int interval) {
        // 間隔の設定がある場合のみ
        if (interval <= 0) {
            return true;
        }

        // 自動立ち上げを最後に実施した日時からの間隔を判定
        if (lastDateTimeAutoLaunch == null) {
            lastDateTimeAutoLaunch = new Date();
            return true;
        } else {
            // 現在時刻との比較
            Date current = new Date();
            int diff = DateUtils.differenceMinutes(current,
                    lastDateTimeAutoLaunch);

            // 定義された間隔以下であれば自動配信を行わないため処理を終了する
            // intervalは分単位で定義されている
            if (diff < interval) {
                log.info("前回のプロジェクト自動立ち上げから[" + interval
                        + "]分が経過していないためプロジェクト自動立ち上げを行いません");
                return false;
            } else {
                // lastDateTimeAutoLaunchの更新
                lastDateTimeAutoLaunch = current;
                return true;
            }
        }
    }

    /**
     * 設定された時間間隔(分単位)によりプロジェクト自動送信を行なってよいかを判定します。
     * 
     * @param rule
     *            解析ルールインスタンス
     * @return boolean true:自動送信許可 false:自動送信不可
     */
    boolean isAutoSendByInterval(int interval) {
        // 間隔の設定がある場合のみ実施
        if (interval <= 0) {
            // 設定なしの場合は常にtrueを返却
            return true;
        }

        // 自動配信を最後に送信した日時からの間隔を判定
        if (lastDateTimeAutoSend == null) {
            lastDateTimeAutoSend = new Date();
            return true;
        } else {
            // 現在時刻との比較
            Date current = new Date();
            int diff = DateUtils.differenceMinutes(current,
                    lastDateTimeAutoSend);

            // 定義された間隔以下であれば自動配信を行わないため処理を終了する
            // intervalは分単位で定義されている
            if (diff < interval) {
                log.info("前回のプロジェクト自動配信から[" + interval
                        + "]分が経過していないためプロジェクト自動配信を行いません");
                return false;
            } else {
                // lastDateTimeAutoLaunchの更新
                lastDateTimeAutoSend = current;
                return true;
            }
        }
    }

}
