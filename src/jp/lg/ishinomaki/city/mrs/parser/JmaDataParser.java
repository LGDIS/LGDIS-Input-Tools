//
//  JAlertKishouDataParser.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.xpath.XPathConstants;

import jp.lg.ishinomaki.city.mrs.utils.StringUtils;

import org.dom4j.CDATA;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * J-Alertから送信されたXMLデータを解析します。
 * 
 */
public class JmaDataParser {

    /**
     * 当クラスのロガーインスタンス
     */
    private final Logger log = Logger.getLogger(JmaDataParser.class
            .getSimpleName());

    // -------------------------------------------------------------------------
    // XML解析結果を保持するインスタンス変数
    // -------------------------------------------------------------------------
    /**
     * トラッカーID.<br>
     * XMLから取得したInformationTypeをキーにparse_rule.xmlからtracker_idを取得
     */
    private String trackerId;

    /**
     * プロジェクトID.<br>
     */
    private String projectId;

    /**
     * プロジェクト自動立ち上げ用フラグ
     */
    private boolean isAutoLaunch;

    /**
     * プロジェクト自動配信用フラグ
     */
    private boolean isAutoSend;

    /**
     * 自動配信先のIDリスト
     */
    private List<String> sendTargetIds = new ArrayList<String>();

    /**
     * カスタムフィールドマップ
     */
    private Map<String, String> customFieldMap = new HashMap<String, String>();

    /**
     * Control部以下のXML内容を文字列として保持
     */
    private String xml_control;

    /**
     * Head部以下のXML内容を文字列として保持
     */
    private String xml_head;

    /**
     * Body部以下のXML内容を文字列として保持
     */
    private String xml_body;

    /**
     * コンストラクタです。
     */
    public JmaDataParser() {
    }

    /**
     * XMLを解析します。
     * 
     * @param data
     * @return
     */
    public boolean parse(String xml) {
        System.out.println(xml);
        try {
            // ------------------------------------------------------------------------
            // Control,Head,Body部抽出
            // ------------------------------------------------------------------------
            Document doc = DocumentHelper.parseText(xml);
            Element controlElement = null;
            Element headElement = null;
            Element bodyElement = null;
            // Root要素(Report)取得
            Element rootElement = doc.getRootElement();
            // Root配下の3要素(Control,Head,Body)取得
            @SuppressWarnings("unchecked")
            List<Element> elements = rootElement.elements();
            for (Element anElement : elements) {
                // Control部以下を全て取得
                if (anElement.getName().equals("Control")) {
                    controlElement = anElement;
                    xml_control = controlElement.asXML();
                }
                // Head部以下を全て取得
                else if (anElement.getName().equals("Head")) {
                    headElement = anElement;
                    xml_head = headElement.asXML();
                }
                // Body部以下を全て取得
                else if (anElement.getName().equals("Body")) {
                    bodyElement = anElement;
                    xml_body = bodyElement.asXML();
                }
            }

            // ------------------------------------------------------------------------
            // XPathによるXMLの解析を行う(別メソッドで行う)
            // ------------------------------------------------------------------------
            boolean ret = parseByXpath(xml);
            if (ret == false) {
                log.severe("XMLの解析に失敗しました。");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.severe("XMLの解析に失敗しました。");
            return false;
        }

        return true;
    }

    /**
     * xml解析のうちXPathで解析する部分の処理を行う内部メソッド
     */
    private boolean parseByXpath(String xml) {

        // XPath使用準備
        InputStream bis = new ByteArrayInputStream(xml.getBytes());
        javax.xml.parsers.DocumentBuilder db;
        try {
            db = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(bis);
            javax.xml.xpath.XPath xpath = javax.xml.xpath.XPathFactory
                    .newInstance().newXPath();

            // XML解析ルール取得
            ParseRule rule = ParseRule.getInstance();

            // --------------------------------------------------------
            // トラッカーID取得
            // --------------------------------------------------------
            String informationType = stringByXpath(xpath,
                    rule.getTrackerXpath(), doc);
            trackerId = rule.getTrackerId(informationType);
            if (StringUtils.isBlank(trackerId)) {
                log.warning("トラッカーIDが特定できないため処理を中断します。 Information Type -> "
                        + informationType);
                return false;
            }

            // --------------------------------------------------------
            // プロジェクトID取得
            // --------------------------------------------------------
            String status = stringByXpath(xpath, rule.getProjectXpath(), doc);
            projectId = rule.getProjectId(status);
            if (projectId == null) {
                log.warning("プロジェクトIDが特定できないため処理を中断します。 Status -> " + status);
                return false;
            }

            // --------------------------------------------------------
            // カスタムフィールド取得
            // key:カスタムフィールドID value:Xpathで取得した値
            // の形式にしてcustomFieldMap変数に保持
            // Xpathで値が取得できなかった場合はcusotmFieldMapに保持しない
            // --------------------------------------------------------
            Map<Integer, String> customFieldXpaths = rule.getCustomFields();
            for (Integer customFieldId : customFieldXpaths.keySet()) {
                String customFieldXpath = customFieldXpaths.get(customFieldId);
                // カスタムフィールドに設定する値を取得
                String customFieldValue = stringByXpath(xpath,
                        customFieldXpath, doc);
                if (StringUtils.isBlank(customFieldValue) == false) {
                    customFieldMap.put(String.valueOf(customFieldId),
                            customFieldValue);
                }
            }

            // --------------------------------------------------------
            // プロジェクト自動立ち上げを判定
            // --------------------------------------------------------
            // 震度を取得
            String strSeismicIntensity = stringByXpath(xpath,
                    rule.getSeismicIntensityXpath(), doc);
            // 震度が取得できた場合は震度による自動立ち上げ判定実施
            if (StringUtils.isBlank(strSeismicIntensity) == false) {
                double seismicIntensity = Double
                        .parseDouble(strSeismicIntensity);
                // 自動立ち上げの震度のしきい値取得
                double autoLaunchSeismicIntensityThreshold = rule
                        .getAutoLaunchSeismicIntensityThreashold()
                        .doubleValue();
                // 自動立ち上げを判定
                if (seismicIntensity >= autoLaunchSeismicIntensityThreshold) {
                    isAutoLaunch = true;
                    log.finest("震度:" + strSeismicIntensity + " しきい値:" + autoLaunchSeismicIntensityThreshold + " のため自動立ち上げON");
                }
            }

            // 津波の高さを取得
            String tsunamiHeightPath = rule.getTsunamiHeightXpath();
            List<String> tsunamiHeights = new ArrayList<String>();
            // 複数のノードが返るときはNodeListを使う
            NodeList nodes = nodelistByXpath(xpath, tsunamiHeightPath, doc);
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node textNode = nodes.item(i);
                    tsunamiHeights.add(textNode.getTextContent());
                }
            }
            // 津波の高さが取得できた場合は高さによる自動立ち上げ判定実施
            if (tsunamiHeights.size() > 0 && isAutoLaunch == false) {
                // 津波の高さのしきい値取得
                double autoLaunchTsunamiHeightThreshold = rule
                        .getAutoLaunchTsunamiHeightThreashold().doubleValue();
                for (String sHeight : tsunamiHeights) {
                    double dHeight = Double.parseDouble(sHeight);
                    if (dHeight >= autoLaunchTsunamiHeightThreshold) {
                        isAutoLaunch = true;
                        log.finest("高さ:" + sHeight + " しきい値:" + autoLaunchTsunamiHeightThreshold + " のため自動立ち上げON");
                        break;
                    }
                }
            }

            // --------------------------------------------------------
            // プロジェクト自動配信を判定
            // --------------------------------------------------------
            // 震度が取得できた場合は震度による自動配信判定実施
            if (StringUtils.isBlank(strSeismicIntensity) == false) {
                double seismicIntensity = Double
                        .parseDouble(strSeismicIntensity);
                // 自動配信の震度のしきい値取得
                double autoSendSeismicIntensityThreshold = rule
                        .getAutoSendSeismicIntensityThreashold().doubleValue();
                // 自動立ち上げを判定
                if (seismicIntensity >= autoSendSeismicIntensityThreshold) {
                    isAutoSend = true;
                    log.finest("震度:" + strSeismicIntensity + " しきい値:" + autoSendSeismicIntensityThreshold + " のため自動配信ON");
                }
            }
            // 津波の高さが取得できた場合は高さによる自動配信判定実施
            if (tsunamiHeights.size() > 0 && isAutoSend == false) {
                // 津波の高さのしきい値取得
                double autoSendTsunamiHeightThreshold = rule
                        .getAutoSendTsunamiHeightThreashold().doubleValue();
                for (String sHeight : tsunamiHeights) {
                    double dHeight = Double.parseDouble(sHeight);
                    if (dHeight >= autoSendTsunamiHeightThreshold) {
                        isAutoSend = true;
                        log.finest("高さ:" + sHeight + " しきい値:" + autoSendTsunamiHeightThreshold + " のため自動配信ON");
                        break;
                    }
                }
            }

            // 自動配信先のIDリストを取得
            sendTargetIds = rule.getAutoSendTargets();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Documentからxpathで指定したテキストを取得します.<br>
     * 
     * @param xpath
     * @param path
     * @param doc
     * @return String 取得テキスト xpath上にテキストがない場合はnullを返却
     */
    private String stringByXpath(javax.xml.xpath.XPath xpath, String path,
            org.w3c.dom.Document doc) {
        String ret = null;
        try {
            ret = xpath.evaluate(path, doc);
        } catch (Exception e) {
        }
        return ret;
    }

    /**
     * Documentからxpathで指定したテキストを取得します.<br>
     * 
     * @param xpath
     * @param path
     * @param doc
     * @return String 取得テキスト xpath上にテキストがない場合はnullを返却
     */
    private NodeList nodelistByXpath(javax.xml.xpath.XPath xpath, String path,
            org.w3c.dom.Document doc) {
        NodeList ret = null;
        try {
            ret = (NodeList) xpath.evaluate(path, doc, XPathConstants.NODESET);
        } catch (Exception e) {
        }
        return ret;
    }

    /**
     * Issuesに渡すxmlデータを作成します
     * 
     * @return
     */
    public String createIssuesXmlAsString() {

        Document doc = DocumentHelper.createDocument();

        // ルートは"issue"
        Element issue = doc.addElement("issue");

        // subject設定
        Element subject = issue.addElement("subject");
        subject.addText("データ受信機能テスト"); // for test

        // プロジェクト自動立ち上げフラグがONの場合
        if (isAutoLaunch) {
            Element auto_launch = issue.addElement("auto_launch");
            auto_launch.addText("1");
        } else {
            // 固定のプロジェクトID設定
            // プロジェクト自動立ち上げの場合は設定しない
            Element project_id = issue.addElement("project_id");
            project_id.addText(projectId);
        }

        // プロジェクト自動配信フラグがONの場合
        if (isAutoSend) {
            Element auto_send = issue.addElement("auto_send");
            auto_send.addText("1");
            // 自動配信先を設定
            // カンマ区切りの文字列でIDのリストを設定
            Element auto_send_targets = issue.addElement("auto_send_targets");
            StringBuilder sb = new StringBuilder();
            if (sendTargetIds != null) {
                for (int i = 0, l = sendTargetIds.size(); i < l; i++) {
                    sb.append(sendTargetIds.get(i));
                    if (i != (l - 1)) {
                        sb.append(",");
                    }
                }
            }
            auto_send_targets.addText(sb.toString());
        }

        // トラッカーID設定
        Element tracker_id = issue.addElement("tracker_id");
        tracker_id.addText(trackerId);

        // control部
        Element xml_control_element = issue.addElement("xml_control");
        CDATA controlCDATA = DocumentHelper.createCDATA(xml_control);
        xml_control_element.add(controlCDATA);

        // head部
        Element xml_head_element = issue.addElement("xml_head");
        CDATA headCDATA = DocumentHelper.createCDATA(xml_head);
        xml_head_element.add(headCDATA);

        // body部
        Element xml_body_element = issue.addElement("xml_body");
        CDATA bodyCDATA = DocumentHelper.createCDATA(xml_body);
        xml_body_element.add(bodyCDATA);

        // カスタムフィールド
        Element customFields = issue.addElement("custom_fields");
        customFields.addAttribute("type", "array");
        for (String key : customFieldMap.keySet()) {
            Element cf = customFields.addElement("custom_field");
            cf.addAttribute("id", key);
            Element cfe = cf.addElement("value");
            cfe.addText(customFieldMap.get(key));
        }

        return doc.asXML();
    }

}
