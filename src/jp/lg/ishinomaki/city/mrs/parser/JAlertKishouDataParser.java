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
import java.util.List;
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
public class JAlertKishouDataParser {

    /**
     * 当クラスのロガーインスタンス
     */
    private final Logger log = Logger.getLogger(JAlertKishouDataParser.class
            .getSimpleName());

    // -------------------------------------------------------------------------
    // 入力XMLから取得する項目をインスタンス変数として定義
    // -------------------------------------------------------------------------
    /**
     * トラッカーID.<br>
     * XMLから取得したInformationTypeをキーにparse_rule.xmlからtracker_idを取得
     */
    private String trackerId;

    /**
     * 運用種別<br>
     * "通常","訓練","試験"の値をとる。"訓練"の場合と"試験"の場合はそれぞれ専用のプロジェクトIDを付与する。
     */
    private String status;

    /**
     * 編集官署名
     */
    private String editorialOffice;

    /**
     * 発表官署名
     */
    private String publishingOffice;

    /**
     * 発表時刻
     */
    private String reportDateTime;

    /**
     * 基点時刻
     */
    private String targetDateTime;

    /**
     * 基点時刻のあいまいさ
     */
    private String targetDtDubious;

    /**
     * 
     */
    private String targetDuration;

    /**
     * 失効時刻
     */
    private String validDateTime;

    /**
     * 識別情報
     */
    private String eventId;

    /**
     * 情報形態
     */
    private String infoType;

    /**
     * 情報番号
     */
    private String serial;

    /**
     * スキーマの運用種別情報
     */
    private String infoKind;

    /**
     * スキーマの運用種別情報のバージョン番号
     */
    private String infoKindVersion;

    /**
     * テキスト要素
     */
    private String text;

    /**
     * 地震の震度
     */
    private String seismicIntensity;

    /**
     * 津波の高さ.<br>
     * 複数の値を持つことがあるためList型
     */
    private List<String> tsunamiHeights;

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
    public JAlertKishouDataParser() {
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
            // カスタムフィールド部抽出
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

            // informationTypeにより紐付けられたトラッカーIDを取得
            // トラッカーIDが取得できない場合は処理しない
            String informationType = stringByXpath(xpath,
                    rule.getInformationTypeXpath(), doc);
            trackerId = rule.getTracker(informationType);
            if (StringUtils.isBlank(trackerId)) {
                log.warning("トラッカーIDが特定できないため処理を中断します。 InformationType -> "
                        + informationType);
                return false;
            }

            // 運用種別を取得
            status = stringByXpath(xpath, rule.getStatusXpath(), doc);
            // 編集官署名を取得
            editorialOffice = stringByXpath(xpath,
                    rule.getEditorialOfficeXpath(), doc);
            // 発表官署名を取得
            publishingOffice = stringByXpath(xpath,
                    rule.getPublishingOfficeXpath(), doc);
            // 発表時刻を取得
            reportDateTime = stringByXpath(xpath,
                    rule.getReportDateTimeXpath(), doc);
            // 基点時刻を取得
            targetDateTime = stringByXpath(xpath,
                    rule.getTargetDateTimeXpath(), doc);
            // 基点時刻のあいまいさを取得
            targetDtDubious = stringByXpath(xpath,
                    rule.getTargetDtDubiousXpath(), doc);
            //
            targetDuration = stringByXpath(xpath,
                    rule.getTargetDurationXpath(), doc);
            // 失効時刻を取得
            validDateTime = stringByXpath(xpath, rule.getValidDateTimeXpath(),
                    doc);
            // 識別情報を取得
            eventId = stringByXpath(xpath, rule.getEventIdXpath(), doc);
            // 情報形態を取得
            infoType = stringByXpath(xpath, rule.getInfoTypeXpath(), doc);
            // 情報番号
            serial = stringByXpath(xpath, rule.getSerialXpath(), doc);
            // スキーマの運用種別情報
            infoKind = stringByXpath(xpath, rule.getInfoKindXpath(), doc);
            // スキーマの運用種別情報のバージョン番号
            infoKindVersion = stringByXpath(xpath,
                    rule.getInfoKindVersionXpath(), doc);
            // テキスト要素
            text = stringByXpath(xpath, rule.getTextXpath(), doc);

            // 地震の震度
            seismicIntensity = stringByXpath(xpath,
                    rule.getAutoLaunchSeismicIntensityXpath(), doc);
            // 津波の高さ
            // 複数保持する可能性があるためList型で保持
            String tsunamiHeightPath = rule.getAutoLaunchTsunamiHeightXpath();
            tsunamiHeights = new ArrayList<String>();
            // 複数のノードが返るときはNodeListを使う
            NodeList nodes = nodelistByXpath(xpath, tsunamiHeightPath, doc);
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node textNode = nodes.item(i);
                    tsunamiHeights.add(textNode.getTextContent());
                }
            }

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

        // ルールファイルを使用
        ParseRule rule = ParseRule.getInstance();

        Document doc = DocumentHelper.createDocument();

        // ルートは"issue"
        Element issue = doc.addElement("issue");

        // subject設定
        Element subject = issue.addElement("subject");
        subject.addText("データ受信機能テスト");  // for test

        // ----------------------------------------------------------------
        // プロジェクト自動立ち上げ判定
        // ----------------------------------------------------------------
        boolean isAutoLaunch = false;
        // 震度による判断
        if (StringUtils.isBlank(seismicIntensity) == false) {
            Double threashold = rule.getAutoLaunchSeismicIntensityThreashold();
            if (threashold != null) {
                double dThreashold = threashold.doubleValue();
                double value = Double.valueOf(seismicIntensity);
                log.finest("地震時のプロジェクト自動立ち上げ判定");
                log.finest("震度のしきい値 -> " + dThreashold);
                log.finest("実際の震度 -> " + value);
                if (value > dThreashold) {
                    log.finest("プロジェクト自動立ち上げします");
                    isAutoLaunch = true;
                }
            }
        }
        // 津波の高さによる判断
        // 震度によるプロジェクト自動立ち上げ判定でONの場合は下記処理はスキップする
        if (tsunamiHeights != null && tsunamiHeights.size() > 0 && isAutoLaunch == false) {
            Double threashold = rule.getAutoLaunchTsunamiHeightThreashold();
            if (threashold != null) {
                double dThreashold = threashold.doubleValue();
                log.finest("津波時のプロジェクト自動立ち上げ判定");
                log.finest("高さのしきい値 -> " + dThreashold);
                for (String height : tsunamiHeights) {
                    double value = Double.valueOf(height);
                    log.finest("実際の高さ -> " + value);
                    if (value > dThreashold) {
                        log.finest("プロジェクト自動立ち上げします");
                        isAutoLaunch = true;
                        break;
                    }
                }
            }
        }
        // プロジェクト自動立ち上げフラグがONの場合
        if (isAutoLaunch) {
            Element auto_launch = issue.addElement("auto_launch");
            auto_launch.addText("1");
        } else {
            // 固定のプロジェクトID設定
            // プロジェクト自動立ち上げの場合は設定しない
            Element project_id = issue.addElement("project_id");
            // 訓練の場合は訓練用のプロジェクトID設定
            if (status.equals("訓練")) {
                project_id.addText(rule.getTrainingProjectId());
            }
            // 試験の場合は通信テストフラグ設定
            else if (status.equals("試験")) {
                project_id.addText(rule.getTestProjectId());
            }
            // 上記以外は固定のプロジェクトID設定
            else {
                project_id.addText(rule.getJmaProjectId());
            }
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
        // statusを設定
        if (!StringUtils.isBlank(status)) {
            Element statusField = customFields.addElement("custom_field");
            statusField.addAttribute("id", "1");
            statusField.addText(status);
        }
        // editorialOfficeを設定
        if (!StringUtils.isBlank(editorialOffice)) {
            Element editorialOfficeField = customFields
                    .addElement("custom_field");
            editorialOfficeField.addAttribute("id", "2");
            editorialOfficeField.addText(editorialOffice);
        }
        // publishingOfficeを設定
        if (!StringUtils.isBlank(publishingOffice)) {
            Element publishingOfficeField = customFields
                    .addElement("custom_field");
            publishingOfficeField.addAttribute("id", "3");
            publishingOfficeField.addText(publishingOffice);
        }
        // reportDateTimeを設定
        if (!StringUtils.isBlank(reportDateTime)) {
            Element reportDateTimeField = customFields
                    .addElement("custom_field");
            reportDateTimeField.addAttribute("id", "4");
            reportDateTimeField.addText(reportDateTime);
        }
        // targetDateTimeを設定
        if (!StringUtils.isBlank(targetDateTime)) {
            Element targetDateTimeField = customFields
                    .addElement("custom_field");
            targetDateTimeField.addAttribute("id", "5");
            targetDateTimeField.addText(targetDateTime);
        }
        // targetDtDubiousを設定
        if (!StringUtils.isBlank(targetDtDubious)) {
            Element targetDtDubiousField = customFields
                    .addElement("custom_field");
            targetDtDubiousField.addAttribute("id", "6");
            targetDtDubiousField.addText(targetDtDubious);
        }
        // targetDurationを設定
        if (!StringUtils.isBlank(targetDuration)) {
            Element targetDtDubiousField = customFields
                    .addElement("custom_field");
            targetDtDubiousField.addAttribute("id", "7");
            targetDtDubiousField.addText(targetDuration);
        }
        // validDateTimeを設定
        if (!StringUtils.isBlank(validDateTime)) {
            Element validDateTimeField = customFields
                    .addElement("custom_field");
            validDateTimeField.addAttribute("id", "8");
            validDateTimeField.addText(validDateTime);
        }
        // eventIdを設定
        if (!StringUtils.isBlank(eventId)) {
            Element eventIdField = customFields.addElement("custom_field");
            eventIdField.addAttribute("id", "9");
            eventIdField.addText(eventId);
        }
        // infoTypeを設定
        if (!StringUtils.isBlank(infoType)) {
            Element infoTypeField = customFields.addElement("custom_field");
            infoTypeField.addAttribute("id", "10");
            infoTypeField.addText(infoType);
        }
        // serialを設定
        if (!StringUtils.isBlank(serial)) {
            Element serialField = customFields.addElement("custom_field");
            serialField.addAttribute("id", "11");
            serialField.addText(serial);
        }
        // infoKindを設定
        if (!StringUtils.isBlank(infoKind)) {
            Element infoKindField = customFields.addElement("custom_field");
            infoKindField.addAttribute("id", "12");
            infoKindField.addText(serial);
        }
        // infoKindVersionを設定
        if (!StringUtils.isBlank(infoKindVersion)) {
            Element infoKindVersionField = customFields
                    .addElement("custom_field");
            infoKindVersionField.addAttribute("id", "13");
            infoKindVersionField.addText(infoKindVersion);
        }
        // Textを設定
        if (!StringUtils.isBlank(text)) {
            Element textField = customFields.addElement("custom_field");
            textField.addAttribute("id", "14");
            textField.addText(text);
        }

        return doc.asXML();
    }

}
