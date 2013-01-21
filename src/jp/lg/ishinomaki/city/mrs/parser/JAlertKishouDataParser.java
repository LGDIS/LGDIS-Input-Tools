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
import java.util.List;
import java.util.logging.Logger;

import org.dom4j.CDATA;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

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

    /**
     * トラッカーID
     */
    private String trackerId;

    /**
     * プロジェクトID
     */
    private String projectId;

    private String status;

    private String editorialOffice;

    private String publishingOffice;

    private String reportDateTime;

    private String targetDateTime;

    private String targetDtDubious;

    private String targetDuration;

    private String validDateTime;

    private String eventId;

    private String infoType;

    private String serial;

    private String infoKind;

    private String infoKindVersion;

    private String text;

    private String cause;

    private String apply;

    private String xml_control;

    private String xml_head;

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
                if (anElement.getName().equals("Control")) {
                    controlElement = anElement;
                    xml_control = controlElement.asXML();
                } else if (anElement.getName().equals("Head")) {
                    headElement = anElement;
                    xml_head = headElement.asXML();
                } else if (anElement.getName().equals("Body")) {
                    bodyElement = anElement;
                    xml_body = bodyElement.asXML();
                }
            }

            // ------------------------------------------------------------------------
            // カスタムフィールド部抽出
            // ------------------------------------------------------------------------
            parseByXpath(xml);

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
    private void parseByXpath(String xml) {

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

            editorialOffice = stringByXpath(xpath,
                    rule.getEditorialOfficeXpath(), doc);
            publishingOffice = stringByXpath(xpath,
                    rule.getPublishingOfficeXpath(), doc);
            reportDateTime = stringByXpath(xpath,
                    rule.getReportDateTimeXpath(), doc);
            targetDateTime = stringByXpath(xpath,
                    rule.getTargetDateTimeXpath(), doc);
            targetDtDubious = stringByXpath(xpath,
                    rule.getTargetDtDubiousXpath(), doc);
            targetDuration = stringByXpath(xpath,
                    rule.getTargetDurationXpath(), doc);
            validDateTime = stringByXpath(xpath, rule.getValidDateTimeXpath(),
                    doc);
            eventId = stringByXpath(xpath, rule.getEventIdXpath(), doc);
            infoType = stringByXpath(xpath, rule.getInfoTypeXpath(), doc);
            serial = stringByXpath(xpath, rule.getSerialXpath(), doc);
            infoKind = stringByXpath(xpath, rule.getInfoKindXpath(), doc);
            infoKindVersion = stringByXpath(xpath,
                    rule.getInfoKindVersionXpath(), doc);
            text = stringByXpath(xpath, rule.getTextXpath(), doc);

            String informationType = stringByXpath(xpath,
                    rule.getInformationTypeXpath(), doc);
            String trackerXpath = rule.getTracker(informationType);
            trackerId = stringByXpath(xpath, trackerXpath, doc);

            projectId = rule.getJmaProjectId();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        subject.addText("データ受信機能テスト");

        // プロジェクトID設定
        Element project_id = issue.addElement("project_id");
        project_id.addText(projectId);

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
        if (status != null) {
            Element statusField = customFields.addElement("custom_field");
            statusField.addAttribute("id", "1");
            statusField.addText(status);
        }
        // editorialOfficeを設定
        if (editorialOffice != null) {
            Element editorialOfficeField = customFields
                    .addElement("custom_field");
            editorialOfficeField.addAttribute("id", "2");
            editorialOfficeField.addText(editorialOffice);
        }
        // publishingOfficeを設定
        if (publishingOffice != null) {
            Element publishingOfficeField = customFields
                    .addElement("custom_field");
            publishingOfficeField.addAttribute("id", "3");
            publishingOfficeField.addText(publishingOffice);
        }
        // reportDateTimeを設定
        if (reportDateTime != null) {
            Element reportDateTimeField = customFields
                    .addElement("custom_field");
            reportDateTimeField.addAttribute("id", "4");
            reportDateTimeField.addText(reportDateTime);
        }
        // targetDateTimeを設定
        if (targetDateTime != null) {
            Element targetDateTimeField = customFields
                    .addElement("custom_field");
            targetDateTimeField.addAttribute("id", "5");
            targetDateTimeField.addText(targetDateTime);
        }
        // targetDtDubiousを設定
        if (targetDtDubious != null) {
            Element targetDtDubiousField = customFields
                    .addElement("custom_field");
            targetDtDubiousField.addAttribute("id", "6");
            targetDtDubiousField.addText(targetDtDubious);
        }
        // targetDurationを設定
        if (targetDuration != null) {
            Element targetDtDubiousField = customFields
                    .addElement("custom_field");
            targetDtDubiousField.addAttribute("id", "7");
            targetDtDubiousField.addText(targetDuration);
        }
        // validDateTimeを設定
        if (validDateTime != null) {
            Element validDateTimeField = customFields
                    .addElement("custom_field");
            validDateTimeField.addAttribute("id", "8");
            validDateTimeField.addText(validDateTime);
        }
        // eventIdを設定
        if (eventId != null) {
            Element eventIdField = customFields.addElement("custom_field");
            eventIdField.addAttribute("id", "9");
            eventIdField.addText(eventId);
        }
        // infoTypeを設定
        if (infoType != null) {
            Element infoTypeField = customFields.addElement("custom_field");
            infoTypeField.addAttribute("id", "10");
            infoTypeField.addText(infoType);
        }
        // serialを設定
        if (serial != null) {
            Element serialField = customFields.addElement("custom_field");
            serialField.addAttribute("id", "11");
            serialField.addText(serial);
        }
        // infoKindを設定
        if (infoKind != null) {
            Element infoKindField = customFields.addElement("custom_field");
            infoKindField.addAttribute("id", "12");
            infoKindField.addText(serial);
        }
        // infoKindVersionを設定
        if (infoKindVersion != null) {
            Element infoKindVersionField = customFields
                    .addElement("custom_field");
            infoKindVersionField.addAttribute("id", "13");
            infoKindVersionField.addText(infoKindVersion);
        }
        // Textを設定
        if (text != null) {
            Element textField = customFields.addElement("custom_field");
            textField.addAttribute("id", "14");
            textField.addText(text);
        }
        
        return doc.asXML();
    }

}
