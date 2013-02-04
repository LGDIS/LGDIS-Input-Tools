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
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import jp.lg.ishinomaki.city.mrs.utils.StringUtils;

import org.w3c.dom.Document;
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
     * issue拡張データマップ
     */
    private Map<String, String> issueExtraMap = new HashMap<String, String>();

    /**
     * カスタムフィールドマップ
     */
    private Map<String, String> customFieldMap = new HashMap<String, String>();

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
    public JmaDataParser() {
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
            // 抽出にはDom4jを使用する
            // ------------------------------------------------------------------------
            org.dom4j.Document doc = org.dom4j.DocumentHelper.parseText(xml);
            org.dom4j.Element controlElement = null;
            org.dom4j.Element headElement = null;
            org.dom4j.Element bodyElement = null;
            // Root要素(Report)取得
            org.dom4j.Element rootElement = doc.getRootElement();
            // Root配下の3要素(Control,Head,Body)取得
            @SuppressWarnings("unchecked")
            List<org.dom4j.Element> elements = rootElement.elements();
            for (org.dom4j.Element anElement : elements) {
                // Control部以下を全て取得
                if (anElement.getName().equals("Control")) {
                    controlElement = anElement;
                    xmlControl = controlElement.asXML();
                }
                // Head部以下を全て取得
                else if (anElement.getName().equals("Head")) {
                    headElement = anElement;
                    xmlHead = headElement.asXML();
                }
                // Body部以下を全て取得
                else if (anElement.getName().equals("Body")) {
                    bodyElement = anElement;
                    xmlBody = bodyElement.asXML();
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
        DocumentBuilder db;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(bis);
            XPath xpath = XPathFactory.newInstance().newXPath();

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
                        issueExtraMap
                                .put(String.valueOf(fieldName), fieldValue);
                    }
                }
            }

            // --------------------------------------------------------
            // カスタムフィールド取得
            // key:カスタムフィールドID value:Xpathで取得した値
            // の形式にしてcustomFieldMap変数に保持
            // Xpathで値が取得できなかった場合はcusotmFieldMapに保持しない
            // --------------------------------------------------------
            Map<Integer, String> customFieldXpaths = rule.getCustomFields();
            if (customFieldXpaths != null) {
                for (Integer customFieldId : customFieldXpaths.keySet()) {
                    String customFieldXpath = customFieldXpaths
                            .get(customFieldId);
                    // カスタムフィールドに設定する値を取得
                    String customFieldValue = stringByXpath(xpath,
                            customFieldXpath, doc);
                    if (StringUtils.isBlank(customFieldValue) == false) {
                        customFieldMap.put(String.valueOf(customFieldId),
                                customFieldValue);
                    }
                }
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
                    log.finest("震度:" + strSeismicIntensity + " しきい値:"
                            + autoLaunchSeismicIntensityThreshold
                            + " のため自動立ち上げON");
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
                        log.finest("高さ:" + sHeight + " しきい値:"
                                + autoLaunchTsunamiHeightThreshold
                                + " のため自動立ち上げON");
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
                    log.finest("震度:" + strSeismicIntensity + " しきい値:"
                            + autoSendSeismicIntensityThreshold + " のため自動配信ON");
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
                        log.finest("高さ:" + sHeight + " しきい値:"
                                + autoSendTsunamiHeightThreshold + " のため自動配信ON");
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
                String basePath = (String) info.get(ParseRule.BASE_PATH);
                String relativePath = (String) info
                        .get(ParseRule.RELATIVE_PATH);
                List<String> remarksPaths = (List<String>) info
                        .get(ParseRule.REMARKS_PATHS);
                String staticRemarksPath = (String) info
                        .get(ParseRule.STATICS_REMARKS_PATH);

                System.out.println("basePath -> " + basePath);
                System.out.println("relativePath -> " + relativePath);
                System.out.println("remarksPaths ->" + remarksPaths);
                System.out.println("staticRemarksPath -> " + staticRemarksPath);
                // まずはベースとなるNodeListを取得
                NodeList nodes = nodelistByXpath(xpath, basePath, doc);
                // NodeListが取得できない場合は次のデータへ
                if (nodes == null) {
                    System.out.println("basePathでNodeのリストが取得できないため処理中断");
                    continue;
                }
                System.out.println("basePathで取得したNodeのリスト数 -> "
                        + nodes.getLength());

                // 取得した全てのNodeに対して処理を行う
                for (int i = 0; i < nodes.getLength(); i++) {
                    // 1つのNodeを取得して処理
                    Node aNode = nodes.item(i);
                    System.out.println("[ループ1]----------------------------");
                    System.out.println("[ループ1] 処理対象のNode -> "
                            + aNode.getNodeName());

                    // 相対パスでGeo情報取得
                    if (StringUtils.isBlank(relativePath) == false) {
                        System.out.println("[ループ1] 相対パス指定あり");
                        // 相対パスでNodeのリストを取得
                        NodeList relativeNodes = nodelistByXpath(xpath,
                                relativePath, aNode);
                        System.out
                                .println("[ループ1] relativePathで取得したNodeのリスト数 -> "
                                        + relativeNodes.getLength());
                        // 上記で取得した全てのNodeに対してvalueと@datumを取得
                        for (int j = 0; j < relativeNodes.getLength(); j++) {
                            // Nodeに対してValueと@datumを取得
                            Node aRelativeNode = relativeNodes.item(j);
                            String val = stringByXpath(xpath, "text()",
                                    aRelativeNode);
                            System.out
                                    .println("[ループ2]----------------------------");
                            System.out.println("[ループ2] 処理対象のNode -> "
                                    + aRelativeNode.getNodeName());
                            System.out.println("[ループ2] 処理対象のNodeValue -> "
                                    + val);
                            if (StringUtils.isBlank(val)) {
                                System.out
                                        .println("[ループ2] NodeValueがNullのためループ終了");
                                continue;
                            }

                            // 測地系情報取得
                            String datum = stringByXpath(xpath, "@datum",
                                    aRelativeNode);
                            System.out.println("[ループ2] 測地系情報(datum) -> "
                                    + datum);

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
                                System.out
                                        .println("[ループ2] 固定の備考文字がないためXMLから備考文字を取得");
                                // 相対パスで備考文字列取得
                                for (String anRemarksPath : remarksPaths) {
                                    System.out.println("[ループ2] 備考文字取得用パス -> "
                                            + anRemarksPath);
                                    // 相対パスで備考文字列取得
                                    String anRemarks = stringByXpath(xpath,
                                            anRemarksPath, aNode);
                                    System.out.println("[ループ2] 備考文字列 -> "
                                            + anRemarks);
                                    if (StringUtils.isBlank(anRemarks) == false) {
                                        if (StringUtils.isBlank(remarks)) {
                                            remarks = anRemarks;
                                        } else {
                                            // 備考文字列は複数ある場合に半角スペースで連結する
                                            remarks = remarks + " " + anRemarks;
                                        }
                                        System.out
                                                .println("[ループ2] 連結後の備考文字列 -> "
                                                        + remarks);
                                    }
                                }
                            } else {
                                remarks = stringByXpath(xpath,
                                        staticRemarksPath, aNode);
                                System.out.println("[ループ2] 固定の備考文字列 -> "
                                        + remarks);
                            }
                            // Map型に格納してissueGeographyMapに追加
                            Map<String, String> map = new HashMap<String, String>();
                            // 位置情報をRest用に変換して格納
                            if (geoKey.equals("point")) {
                                val = this.convertPoint(val);
                            } else if (geoKey.equals("polygon")) {
                                val = this.convertPolygon(val);
                            } else if (geoKey.equals("line")) {
                                val = this.convertLine(val);
                            }
                            map.put(geoKey, val);
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
                    // 相対パスの設定がない場合はbasePathからGeo情報取得
                    else {
                        System.out.println("[ループ1] 相対パス指定なし");
                        String val = stringByXpath(xpath, "text()", aNode);
                        System.out.println("[ループ1] 取得したNodeValue -> " + val);

                        // Geo情報が設定されていない場合は次のデータへ
                        if (StringUtils.isBlank(val)) {
                            continue;
                        }
                        // 測地系情報取得
                        String datum = stringByXpath(xpath, "@datum", aNode);
                        System.out.println("[ループ1] 測地系情報(datum) -> " + datum);

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
                            System.out
                                    .println("[ループ1] 固定の備考文字がないためXMLから備考文字を取得");
                            // 相対パスで備考文字列取得
                            for (String anRemarksPath : remarksPaths) {
                                System.out.println("[ループ1] 備考文字取得用パス -> "
                                        + anRemarksPath);
                                // 相対パスで備考文字列取得
                                String anRemarks = stringByXpath(xpath,
                                        anRemarksPath, aNode);
                                System.out.println("[ループ1] 備考文字列 -> "
                                        + anRemarks);
                                if (StringUtils.isBlank(anRemarks) == false) {
                                    if (StringUtils.isBlank(remarks)) {
                                        remarks = anRemarks;
                                    } else {
                                        // 備考文字列は複数ある場合に半角スペースで連結する
                                        remarks = remarks + " " + anRemarks;
                                    }
                                }
                                System.out.println("[ループ1] 連結後の備考文字列 -> "
                                        + remarks);
                            }
                        } else {
                            remarks = stringByXpath(xpath, staticRemarksPath,
                                    aNode);
                        }
                        // Map型に格納してissueGeographyMapに追加
                        Map<String, String> map = new HashMap<String, String>();
                        // 位置情報をRest用に変換して格納
                        if (geoKey.equals("point")) {
                            val = this.convertPoint(val);
                        } else if (geoKey.equals("polygon")) {
                            val = this.convertPolygon(val);
                        } else if (geoKey.equals("line")) {
                            val = this.convertLine(val);
                        }
                        map.put(geoKey, val);
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("解析後の地理系情報 -> " + issueGeographyMaps);
    }

    /**
     * Documentからxpathで指定したテキストを取得します.<br>
     * 
     * @param xpath
     * @param path
     * @param doc
     * @return String 取得テキスト xpath上にテキストがない場合はnullを返却
     */
    private String stringByXpath(XPath xpath, String path, Object item) {
        String ret = null;
        try {
            ret = xpath.evaluate(path, item);
        } catch (Exception e) {
            // ログは不要
        }
        return ret;
    }

    /**
     * Documentからxpathで指定したNodeのリストを取得します.<br>
     * 
     * @param xpath
     * @param path
     * @param doc
     * @return NodeList Nodeのリスト xpath上にテキストがない場合はnullを返却
     */
    private NodeList nodelistByXpath(XPath xpath, String path, Object item) {
        NodeList ret = null;
        try {
            ret = (NodeList) xpath.evaluate(path, item, XPathConstants.NODESET);
        } catch (Exception e) {
            // ログは不要
        }
        return ret;
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

    public List<String> getSendTargetIds() {
        return sendTargetIds;
    }

    public Map<String, String> getIssueExtraMap() {
        return issueExtraMap;
    }

    public Map<String, String> getCustomFieldMap() {
        return customFieldMap;
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

    /**
     * point情報をRest用文字列に変換する内部メソッド.<br>
     * +21.2+135.5/ -> (135.5,21.2)<br>
     * 深さの値は捨てる<br>
     * 
     * @return
     */
    String convertPoint(String val) {
        if (StringUtils.isBlank(val)) {
            return null;
        }
        // 正規表現を利用
        Pattern p = Pattern.compile("([¥+¥-][0-9.]+)");
        Matcher m = p.matcher(val);
        String lat = null;
        String lng = null;
        // 1つ目の要素は緯度
        if (m.find()) {
            lat = m.group();
        } else {
            return null;
        }
        // 2つ目の要素は経度
        if (m.find()) {
            lng = m.group();
        } else {
            return null;
        }
        // !3つ目に深さの要素が存在する場合があるが深さは使用しないため無視する

        // (+136,+35)形式の文字列を作成
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(lng).append(",").append(lat).append(")");
        return sb.toString();
    }

    /**
     * line情報をRest用文字列に変換する内部メソッド.<br>
     * +35+138/+36+138/+37+138/+38+138/ -> [(138,35),(36,138),(37,138),(38,138)]<br>
     * 深さの値は捨てる<br>
     * 
     * @return
     */
    String convertLine(String val) {
        return convertPointArray(val, "[", "]");
    }

    /**
     * point情報をRest用文字列に変換する内部メソッド.<br>
     * +35+138/+36+138/+37+138/+38+138/ -> ((138,35),(138,36),(138,37),(138,38))<br>
     * 深さの値は捨てる<br>
     * 
     * @return
     */
    String convertPolygon(String val) {
        return convertPointArray(val, "(", ")");
    }

    /**
     * 
     * @param val
     *            +35+135/+36+136/
     * @param beginMark
     *            "["や"("など
     * @param endMark
     *            "]"や")"など
     * @return ((+135,+35),(+136,+36))
     */
    String convertPointArray(String val, String beginMark, String endMark) {
        if (StringUtils.isBlank(val) || StringUtils.isBlank(beginMark)
                || StringUtils.isBlank(endMark)) {
            return null;
        }

        // この変数内に地理系情報(文字列)を格納する
        ArrayList<String> points = new ArrayList<String>();

        // 位置情報を"/"で区切る
        StringTokenizer slashTokenizer = new StringTokenizer(val, "/");
        while (slashTokenizer.hasMoreTokens()) {
            String point = slashTokenizer.nextToken();
            String convertedPoint = convertPoint(point);
            points.add(convertedPoint);
        }

        if (points.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(beginMark);
            for (int i = 0; i < points.size(); i++) {
                sb.append(points.get(i));
                if (i != (points.size() - 1)) {
                    sb.append(",");
                }
            }
            sb.append(endMark);

            return sb.toString();
        }
        return null;
    }

}
