//
//  JmaXmlDataParser.java
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import jp.lg.ishinomaki.city.mrs.utils.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Jma,J-Alertから送信された気象情報XMLデータを解析します。
 * 
 */
public class JmaXmlDataParser extends XmlDataParser {

    /**
     * 当クラスのロガーインスタンス
     */
    private final Logger log = Logger.getLogger(JmaXmlDataParser.class
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
     * issue拡張データマップ
     */
    private Map<String, String> issueExtraMap = new HashMap<String, String>();

    /**
     * issue_geographyに格納するデータのリスト
     */
    private List<Map<String, String>> issueGeographyMaps = new ArrayList<Map<String, String>>();

    /**
     * Control部以下のXML内容を文字列として保持
     */
    private String xmlControl;

    /**
     * Head部以下のXML内容を文字列として保持
     */
    private String xmlHead;

    /**
     * Body部以下のXML内容を文字列として保持
     */
    private String xmlBody;

    /**
     * コンストラクタです。
     */
    public JmaXmlDataParser() {
    }

    /**
     * XMLを解析します。
     * 
     * @param data
     * @return
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
            JmaParseRule rule = JmaParseRule.getInstance();

            // Control部
            if (parseXmlControl(doc, xpath, rule) == false) {
                return false;
            }

            // Head部
            if (parseXmlHead(doc, xpath, rule) == false) {
                return false;
            }

            // Body部
            if (parseXmlBody(doc, xpath, rule) == false) {
                return false;
            }

            // トラッカーID
            if (parseTrackerId(doc, xpath, rule) == false) {
                return false;
            }

            // プロジェクトID
            if (parseProjectId(doc, xpath, rule) == false) {
                return false;
            }

            // Issues拡張カラム
            if (parseIssueExtraMap(doc, xpath, rule) == false) {
                return false;
            }

            // --------------------------------------------------------
            // issue_geographies用のデータ取得
            // --------------------------------------------------------
            // Corrdinateの情報をpointに設定
            List<Map<String, Object>> coordinateInfos = rule
                    .getCoordinateInfos();
            parseGeography(coordinateInfos, xpath, doc, "point");

            // Polygonの情報をpolygonに設定
            List<Map<String, Object>> polygonInfos = rule.getPolygonInfos();
            parseGeography(polygonInfos, xpath, doc, "polygon");

            // lineのデータを取得
            List<Map<String, Object>> lineInfos = rule.getLineInfos();
            parseGeography(lineInfos, xpath, doc, "line");

            // Locationのデータを取得
            List<Map<String, Object>> locationInfos = rule.getLocationInfos();
            parseGeography(locationInfos, xpath, doc, "Location");

            // プロジェクト自動立ち上げを判定
            parseIsAutoLaunchBySeismicIntensity(doc, xpath, rule);
            parseIsAutoLaunchByTsunamiHeigh(doc, xpath, rule);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 
     * @param doc
     * @param xpath
     * @param rule
     * @return
     */
    boolean parseXmlControl(Document doc, XPath xpath, JmaParseRule rule) {
        Node node = nodeByXpath(xpath, rule.getXmlControlPath(), doc);
        if (node != null) {
            // XMLを文字列に変換
            try {
                xmlControl = StringUtils.convertToString(node);
            } catch (Exception e) {
                // 例外が発生してもとりあえず処理は続行する
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 
     * @param doc
     * @param xpath
     * @param rule
     * @return
     */
    boolean parseXmlHead(Document doc, XPath xpath, JmaParseRule rule) {
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
     * 
     * @param doc
     * @param xpath
     * @param rule
     * @return
     */
    boolean parseXmlBody(Document doc, XPath xpath, JmaParseRule rule) {
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
     * トラッカーIDを解析する内部メソッド.
     * 
     * @param doc
     * @param xpath
     * @param rule
     * @return
     */
    boolean parseTrackerId(Document doc, XPath xpath, JmaParseRule rule) {
        String informationType = stringByXpath(xpath, rule.getTrackerXpath(),
                doc);
        trackerId = rule.getTrackerId(informationType);
        if (StringUtils.isBlank(trackerId)) {
            log.warning("トラッカーIDが特定できません Information Type -> "
                    + informationType);
            return false;
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
    boolean parseProjectId(Document doc, XPath xpath, JmaParseRule rule) {
        String status = stringByXpath(xpath, rule.getProjectXpath(), doc);
        projectId = rule.getProjectId(status);
        if (projectId == null) {
            log.warning("プロジェクトIDが特定できません Status -> " + status);
            return false;
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
    boolean parseIssueExtraMap(Document doc, XPath xpath, JmaParseRule rule) {
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
     * 震度によるプロジェクト自動立ち上げの判定を行う内部メソッド.
     * 
     * @param doc
     * @param xpath
     * @param rule
     * @return
     */
    boolean parseIsAutoLaunchBySeismicIntensity(Document doc, XPath xpath,
            JmaParseRule rule) {
        // 自動立ち上げの震度のしきい値取得
        String threshold = rule.getAutoLaunchSeismicIntensityThreashold();

        // 震度を取得
        NodeList nodeList = nodelistByXpath(xpath,
                rule.getSeismicIntensityXpath(), doc);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String intensity = node.getNodeValue();
            // 震度がしきい値以上の場合はプロジェクト自動立ち上げフラグON
            if (StringUtils.compareSeismicIntensity(intensity, threshold)) {
                log.finest("震度:" + intensity + " しきい値:" + threshold
                        + " のため自動立ち上げON");
                isAutoLaunch = true;
                return true;
            }
        }
        return true;
    }

    /**
     * 津波の高さによるプロジェクト自動立ち上げの判定を行う内部メソッド.
     * 
     * @param doc
     * @param xpath
     * @param rule
     * @return
     */
    boolean parseIsAutoLaunchByTsunamiHeigh(Document doc, XPath xpath,
            JmaParseRule rule) {
        // 津波の高さを取得
        List<String> tsunamiHeights = parseTsunamiHeights(doc, xpath, rule);
        // 津波の高さが取得できた場合は高さによる自動立ち上げ判定実施
        if (tsunamiHeights.size() > 0) {
            // 津波の高さのしきい値取得
            double autoLaunchTsunamiHeightThreshold = rule
                    .getAutoLaunchTsunamiHeightThreashold().doubleValue();
            for (String sHeight : tsunamiHeights) {
                double dHeight = Double.parseDouble(sHeight);
                if (dHeight >= autoLaunchTsunamiHeightThreshold) {
                    isAutoLaunch = true;
                    log.finest("高さ:" + sHeight + " しきい値:"
                            + autoLaunchTsunamiHeightThreshold + " のため自動立ち上げON");
                    break;
                }
            }
        }
        return true;
    }

    /**
     * XMLから津波の高さを取得し配列で返却する
     * 
     * @param doc
     * @param xpath
     * @param rule
     * @return
     */
    List<String> parseTsunamiHeights(Document doc, XPath xpath,
            JmaParseRule rule) {
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
        return tsunamiHeights;
    }

    /**
     * 震度による自動配信を判定するメソッド.
     * 
     * @param doc
     * @param xpath
     * @param rule
     * @return
     */
    boolean parseIsAutoSendBySeismicIntensity(Document doc, XPath xpath,
            JmaParseRule rule) {
        String threshold = rule.getAutoSendSeismicIntensityThreashold();

        // 震度を取得
        NodeList nodeList = nodelistByXpath(xpath,
                rule.getSeismicIntensityXpath(), doc);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String intensity = node.getNodeValue();
            // 震度がしきい値以上の場合はプロジェクト自動立ち上げフラグON
            if (StringUtils.compareSeismicIntensity(intensity, threshold)) {
                log.finest("震度:" + intensity + " しきい値:" + threshold
                        + " のため自動立ち上げON");
                isAutoSend = true;
                return true;
            }
        }
        return true;
    }

    /**
     * 津波の高さによる自動配信を判定するメソッド.
     * 
     * @param doc
     * @param xpath
     * @param rule
     * @return
     */
    boolean paserIsAutoSendByTsunamiHeight(Document doc, XPath xpath,
            JmaParseRule rule) {
        // 津波の高さを配列で取得
        List<String> tsunamiHeights = parseTsunamiHeights(doc, xpath, rule);

        // 津波の高さが取得できた場合は高さによる自動配信判定実施
        if (tsunamiHeights.size() > 0) {
            // 津波の高さのしきい値取得
            double autoSendTsunamiHeightThreshold = rule
                    .getAutoSendTsunamiHeightThreashold().doubleValue();
            for (String sHeight : tsunamiHeights) {
                double dHeight = Double.parseDouble(sHeight);
                if (dHeight >= autoSendTsunamiHeightThreshold) {
                    isAutoSend = true;
                    log.finest("高さ:" + sHeight + " しきい値:"
                            + autoSendTsunamiHeightThreshold + " のため自動配信ON");
                    break;
                }
            }
        }
        return true;
    }

    /**
     * 位置情報の解析処理を行う内部メソッド
     * 
     * @param infos
     *            位置情報解析ルール(parse_rule.ymlに
     *            "issue_geographyies/coordinate,line,polygon,Location"をキーに定義
     * @param validTypes
     *            typeの妥当性チェック(parse_rule.ymlに"issue_geographyies/valid_types"
     *            をキーに定義)
     * @param xpath
     *            Xpath変数
     * @param doc
     *            ルートからのDocument全体
     * @param geoKey
     *            coordinate or line or polygon or Location
     */
    @SuppressWarnings("unchecked")
    private void parseGeography(List<Map<String, Object>> infos, XPath xpath,
            Document doc, String geoKey) {

        if (infos == null) {
            return;
        }

        try {
            // coordinateで定義されている情報を元に位置系情報取得
            for (Map<String, Object> info : infos) {

                // 取得に使用するXpath取り出し
                String path = (String) info.get(JmaParseRule.PATH);
                List<String> remarksPaths = (List<String>) info
                        .get(JmaParseRule.REMARKS_PATHS);
                String staticRemarksPath = (String) info
                        .get(JmaParseRule.STATICS_REMARKS_PATH);
                String allowType = (String) info.get(JmaParseRule.ALLOW_TYPE);

                System.out.println("path -> " + path);
                System.out.println("remarksPaths ->" + remarksPaths);
                System.out.println("staticRemarksPath -> " + staticRemarksPath);
                System.out.println("allowType -> " + allowType);

                // 地理情報のNode/text()を取得
                // この要素は複数ある可能性があるためNodeListで取得する
                NodeList nodes = nodelistByXpath(xpath, path, doc);
                // NodeListが取得できない場合は次のデータへ
                if (nodes == null) {
                    System.out.println("paathでNodeのリストが取得できないため処理中断");
                    continue;
                }
                System.out
                        .println("pathで取得したNodeのリスト数 -> " + nodes.getLength());

                // 取得した全てのNodeに対して処理を行う
                for (int i = 0; i < nodes.getLength(); i++) {
                    // 1つのNodeを取得して処理
                    Node aNode = nodes.item(i);

                    // まずは該当Nodeを地理情報として使用できるかallowTypeを使用して確認
                    if (allowType != null) {
                        String type = stringByXpath(xpath, "../@type", aNode);
                        if (type != null) {
                            if (!type.equals(allowType)) {
                                // type属性値が許可指定の値と異なるためこのデータはスキップします
                                continue;
                            }
                        }
                    }

                    // まずは地理情報を取得
                    String geoInfo = aNode.getNodeValue();

                    // 次に測地系を取得
                    String datum = stringByXpath(xpath, "../@datum", aNode);

                    // 次に関連する備考文字列を取得
                    // -----------------------------------------------------
                    // 備考用文字列取得
                    // parseRuleにREMARKS_PATHSが設定されている場合は配列に設定
                    // されているXpathを使用して備考文字列を取得する
                    // 設定されていない場合はSTATIC_REMARKS_PATHを使用して
                    // 備考文字列を取得する
                    // -----------------------------------------------------
                    // 固定の備考文字列が設定ファイルに定義されている場合はそれを使用
                    String remarks = null;
                    if (StringUtils.isBlank(staticRemarksPath)) {
                        System.out.println("[ループ2] 固定の備考文字がないためXMLから備考文字を取得");
                        // 相対パスで備考文字列取得
                        for (String anRemarksPath : remarksPaths) {
                            System.out.println("[ループ2] 備考文字取得用パス -> "
                                    + anRemarksPath);
                            // 相対パスで備考文字列取得
                            String anRemarks = stringByXpath(xpath,
                                    anRemarksPath, aNode);
                            System.out.println("[ループ2] 備考文字列 -> " + anRemarks);
                            if (StringUtils.isBlank(anRemarks) == false) {
                                if (StringUtils.isBlank(remarks)) {
                                    remarks = anRemarks;
                                } else {
                                    // 備考文字列は複数ある場合に半角スペースで連結する
                                    remarks = remarks + " " + anRemarks;
                                }
                                System.out.println("[ループ2] 連結後の備考文字列 -> "
                                        + remarks);
                            }
                        }
                    } else {
                        remarks = stringByXpath(xpath, staticRemarksPath, aNode);
                        System.out.println("[ループ2] 固定の備考文字列 -> " + remarks);
                    }
                    // Map型に格納してissueGeographyMapに追加
                    Map<String, String> map = new HashMap<String, String>();
                    String convertedGeoInfo = null;
                    // 位置情報をRest用に座標変換して格納
                    if (geoKey.equals("point")) {
                        convertedGeoInfo = StringUtils.convertPoint(geoInfo);
                    } else if (geoKey.equals("polygon")) {
                        convertedGeoInfo = StringUtils.convertPointArray(geoInfo);
                    } else if (geoKey.equals("line")) {
                        convertedGeoInfo = StringUtils.convertPointArray(geoInfo);
                    } else {
                        // 座標変換の不要なものはそのまま(Location)
                        convertedGeoInfo = geoInfo;
                    }
                    map.put(geoKey, convertedGeoInfo);
                    // 備考情報を格納
                    if (StringUtils.isBlank(remarks) == false) {
                        map.put("remarks", remarks);
                    }
                    // 測地系情報を格納
                    if (StringUtils.isBlank(datum) == false) {
                        map.put("datum", datum);
                    }
                    issueGeographyMaps.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("解析後の地理系情報 -> " + issueGeographyMaps);
    }

    // ----------------------------------------------------
    // 各種変数用のgetterメソッド
    // ----------------------------------------------------

    public String getTrackerId() {
        return trackerId;
    }

    public String getProjectId() {
        return projectId;
    }

    public boolean isAutoLaunch() {
        return isAutoLaunch;
    }

    public boolean isAutoSend() {
        return isAutoSend;
    }

    public Map<String, String> getIssueExtraMap() {
        return issueExtraMap;
    }

    public List<Map<String, String>> getIssueGeographyMaps() {
        return issueGeographyMaps;
    }

    public String getXmlControl() {
        return xmlControl;
    }

    public String getXmlHead() {
        return xmlHead;
    }

    public String getXmlBody() {
        return xmlBody;
    }

}
