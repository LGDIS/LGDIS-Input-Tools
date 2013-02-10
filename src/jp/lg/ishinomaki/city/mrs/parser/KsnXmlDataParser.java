//
//  KsnXmlDataParser.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import jp.lg.ishinomaki.city.mrs.utils.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class KsnXmlDataParser extends XmlDataParser {

    /**
     * 当クラスのロガーインスタンス
     */
    private final Logger log = Logger.getLogger(KsnXmlDataParser.class
            .getSimpleName());

    // -------------------------------------------------------------------------
    // XML解析結果を保持するインスタンス変数
    // -------------------------------------------------------------------------
    /**
     * トラッカーID.<br>
     */
    private String trackerId;

    /**
     * プロジェクトID.<br>
     */
    private String projectId;

    /**
     * issue拡張データマップ
     */
    private Map<String, String> issueExtraMap = new HashMap<String, String>();

    /**
     * Head部以下のXML内容を文字列として保持
     */
    private String xmlHead;

    /**
     * Body部以下のXML内容を文字列として保持
     */
    private String xmlBody;

    /**
     * XML解析メソッド
     */
    @Override
    public boolean parse(String xml) {

        // XPath使用準備
        InputStream bis = new ByteArrayInputStream(xml.getBytes());
        DocumentBuilder db;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(bis);
            XPath xpath = XPathFactory.newInstance().newXPath();

            // XML解析ルール取得
            KsnParseRule rule = KsnParseRule.getInstance();

            // Head部
            if (parseXmlHead(doc, xpath, rule) == false) {
                return false;
            }

            // Body部
            if (parseXmlBody(doc, xpath, rule) == false) {
                return false;
            }

            // プロジェクトID
            if (parseProjectId(doc, xpath, rule) == false) {
                return false;
            }

            // Issue拡張テーブル用定義取得
            parseIssueExtraMap(doc, xpath, rule);

            // トラッカーIDは固定
            trackerId = rule.getTrackerId();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * xmlHead部を取得してXML文字列として保存する
     * 
     * @param doc
     * @param xpath
     * @param rule
     * @return
     */
    boolean parseXmlHead(Document doc, XPath xpath, KsnParseRule rule) {
        Node node = nodeByXpath(xpath, rule.getXmlHeadPath(), doc);
        if (node != null) {
            // XMLを文字列に変換
            try {
                xmlHead = StringUtils.convertToString(node);
            } catch (Exception e) {
                // 例外が発生してもとりあえず処理は続行する
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * xmlBody部を取得してXML文字列として保存する
     * 
     * @param doc
     * @param xpath
     * @param rule
     * @return
     */
    boolean parseXmlBody(Document doc, XPath xpath, KsnParseRule rule) {
        Node node = nodeByXpath(xpath, rule.getXmlBodyPath(), doc);
        if (node != null) {
            // XMLを文字列に変換
            try {
                xmlBody = StringUtils.convertToString(node);
            } catch (Exception e) {
                // 例外が発生してもとりあえず処理は続行する
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Issues拡張カラム用のデータ取得のための内部メソッド.
     * 
     * @param doc
     * @param xpath
     * @param rule
     * @return
     */
    boolean parseIssueExtraMap(Document doc, XPath xpath,
            KsnParseRule rule) {
        // --------------------------------------------------------
        // Issues拡張カラム用のデータ取得
        // key:Issuesテーブルのカラム名 value:Xpathで取得した値
        // の形式にしてissueExtraMap変数に保持
        // Xpathで値が取得できなかった場合はissueExtraMapに保持しない
        // --------------------------------------------------------
        Map<String, String> issueExtraXpaths = rule.getIssueExtras();
        if (issueExtraXpaths != null) {
            for (String fieldName : issueExtraXpaths.keySet()) {
                String fieldXpath = issueExtraXpaths.get(fieldName);
                // Issues拡張フィールドに設定する値を取得
                String fieldValue = stringByXpath(xpath, fieldXpath, doc);
                if (StringUtils.isBlank(fieldValue) == false) {
                    issueExtraMap.put(String.valueOf(fieldName), fieldValue);
                }
            }
        }
        return true;
    }

    /**
     * プロジェクトIDを解析する内部メソッド.
     * 
     * @param doc
     * @param xpath
     * @param rule
     * @return
     */
    boolean parseProjectId(Document doc, XPath xpath, KsnParseRule rule) {
        String status = stringByXpath(xpath, rule.getProjectXpath(), doc);
        projectId = rule.getProjectId(status);
        if (StringUtils.isBlank(projectId)) {
            log.warning("プロジェクトIDが特定できません Status -> " + status);
            return false;
        }
        return true;
    }

    public String getTrackerId() {
        return trackerId;
    }

    public String getProjectId() {
        return projectId;
    }

    public Map<String, String> getIssueExtraMap() {
        return issueExtraMap;
    }

    public String getXmlHead() {
        return xmlHead;
    }

    public String getXmlBody() {
        return xmlBody;
    }

}
