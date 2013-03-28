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
     * プロジェクト自動配信時に設定する○号配備を示す値.<br>
     * 0 -> 0号配備, 1 -> 1号配備, 2 -> 2号配備, 3 -> 3号配備
     */
    private String disposition;

    /**
     * プロジェクト自動送信用フラグ
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
     * 入力元識別子
     */
    private String inputId;

    /**
     * 自動送信時の付随情報
     */
    private Map<String, String> autoSendExtras;

    /**
     * 説明文
     */
    private String description;
    
    /**
     * コンストラクタです。
     */
    public JmaXmlDataParser(String inputId) {
        this.inputId = inputId;
    }

    /**
     * XMLを解析します。
     * 
     * @param data
     *            XML文書全体の文字列
     * @return boolean true:解析成功 false:解析失敗
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
            parseGeography(locationInfos, xpath, doc, "location");

            // --------------------------------------------------------
            // プロジェクト自動立ち上げ/自動送信用データ取得
            // --------------------------------------------------------
            // プロジェクト自動立ち上げを判定
            parseIsAutoLaunchBySeismicIntensity(doc, xpath, rule);
            parseIsAutoLaunchByTsunamiHeight(doc, xpath, rule);

            // プロジェクト自動送信を解析
            parseDisposition(doc, xpath, rule);

            // プロジェクト自動送信の場合の付随情報を設定
            if (isAutoSend == true) {
                autoSendExtras = rule.getAutosendExtras();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Control部の解析処理を行う内部メソッド
     * 
     * @param doc
     *            Document全体
     * @param xpath
     *            XPathインスタンス
     * @param rule
     *            解析ルールインスタンス
     * @return boolean true:解析成功 false:解析失敗
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
     * Head部の解析処理を行う内部メソッド
     * 
     * @param doc
     *            Document全体
     * @param xpath
     *            XPathインスタンス
     * @param rule
     *            解析ルールインスタンス
     * @return boolean true:解析成功 false:解析失敗
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
     * Body部の解析処理を行う内部メソッド
     * 
     * @param doc
     *            Document全体
     * @param xpath
     *            XPathインスタンス
     * @param rule
     *            解析ルールインスタンス
     * @return boolean true:解析成功 false:解析失敗
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
     *            Document全体
     * @param xpath
     *            XPathインスタンス
     * @param rule
     *            解析ルールインスタンス
     * @return boolean true:解析成功 false:解析失敗
     */
    boolean parseTrackerId(Document doc, XPath xpath, JmaParseRule rule) {
        String informationType = stringByXpath(xpath,
                rule.getTrackerXpath(inputId), doc);
        trackerId = rule.getTrackerId(inputId, informationType);
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
        if (StringUtils.isBlank(projectId)) {
            log.warning("プロジェクトIDが特定できません Status -> " + status);
            return false;
        }
        return true;
    }

    /**
     * Issues拡張カラム用のデータ取得のための内部メソッド.
     * 
     * @param doc
     *            Document全体
     * @param xpath
     *            XPathインスタンス
     * @param rule
     *            解析ルールインスタンス
     * @return boolean true:解析成功 false:解析失敗
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
     *            Document全体
     * @param xpath
     *            XPathインスタンス
     * @param rule
     *            解析ルールインスタンス
     * @return boolean true:解析成功 false:解析失敗
     */
    boolean parseIsAutoLaunchBySeismicIntensity(Document doc, XPath xpath,
            JmaParseRule rule) {

        // 自動立ち上げの震度のしきい値取得
        String threshold = rule.getSeismicIntensityThreashold();

        // 震度を取得
        NodeList nodeList = nodelistByXpath(xpath,
                rule.getAutoLaunchSeismicIntensityXpath(), doc);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String intensity = node.getNodeValue();
            // 震度がしきい値以上の場合はプロジェクト自動立ち上げフラグON
            if (StringUtils.compareSeismicIntensity(intensity, threshold)) {
                log.finest("震度:" + intensity + " しきい値:" + threshold);
                isAutoLaunch = true;
                break;
            }
        }
        return true;
    }

    /**
     * 津波の高さによるプロジェクト自動立ち上げの判定を行う内部メソッド.
     * 
     * @param doc
     *            Document全体
     * @param xpath
     *            XPathインスタンス
     * @param rule
     *            解析ルールインスタンス
     * @return boolean true:解析成功 false:解析失敗
     */
    boolean parseIsAutoLaunchByTsunamiHeight(Document doc, XPath xpath,
            JmaParseRule rule) {

        // 津波の高さを取得
        String targetLocation = rule.getAutoLaunchTsunamiHeightXpath();
        List<String> tsunamiHeights = parseTsunamiHeights(doc, xpath, rule,
                targetLocation);
        // 津波の高さが取得できた場合は高さによる自動立ち上げ判定実施
        if (tsunamiHeights.size() > 0) {
            // 津波の高さのしきい値取得
            double autoLaunchTsunamiHeightThreshold = rule
                    .getTsunamiHeightThreashold().doubleValue();
            for (String sHeight : tsunamiHeights) {
                double dHeight = Double.parseDouble(sHeight);
                if (dHeight >= autoLaunchTsunamiHeightThreshold) {
                    log.finest("高さ:" + sHeight + " しきい値:"
                            + autoLaunchTsunamiHeightThreshold);
                    isAutoLaunch = true;
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
     *            Document全体
     * @param xpath
     *            XPathインスタンス
     * @param rule
     *            解析ルールインスタンス
     * @return boolean true:解析成功 false:解析失敗
     */
    List<String> parseTsunamiHeights(Document doc, XPath xpath,
            JmaParseRule rule, String path) {
        // 津波の高さを取得
        List<String> tsunamiHeights = new ArrayList<String>();
        // 複数のノードが返るときはNodeListを使う
        NodeList nodes = nodelistByXpath(xpath, path, doc);
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node textNode = nodes.item(i);
                tsunamiHeights.add(textNode.getTextContent());
            }
        }
        return tsunamiHeights;
    }

    /**
     * 自動配信の配備先を判定するメソッド.
     * 
     * @param doc
     *            Document全体
     * @param xpath
     *            XPathインスタンス
     * @param rule
     *            解析ルールインスタンス
     * @return boolean true:解析成功 false:解析失敗
     */
    @SuppressWarnings("unchecked")
    boolean parseDisposition(Document doc, XPath xpath, JmaParseRule rule) {

        // ○号配備判定用リスト取得
        List<Map<String, Object>> dispositionInfo = rule.getDispositionInfo();

        for (Map<String, Object> anInfo : dispositionInfo) {
            // ○号配備識別子を取得
            String dispositionNumber = (String) anInfo.get(JmaParseRule.NUMBER);
            if (StringUtils.isBlank(dispositionNumber)) {
                continue;
            }

            // ○号配備を判定する値を取得するためのXPathのリストを取得し条件判定
            List<String> paths = (List<String>) anInfo.get(JmaParseRule.PATHS);
            for (String path : paths) {
                // 指定したXPathでNodeが取得できた場合は指定の○号配備識別子を設定する
                Node node = nodeByXpath(xpath, path, doc);
                if (node != null) {
                    disposition = dispositionNumber;
                    break;
                }
            }
        }

        // プロジェクト自動送信判定
        if (StringUtils.isBlank(disposition) == false) {
            // メッセージの作成
            List<Map<String, Object>> sendMessageInfo = rule
                    .getSendMessageInfo();
            for (Map<String, Object> anInfo : sendMessageInfo) {
                String parameter = (String) anInfo.get(JmaParseRule.PARAMETER);
                Integer maxLength = (Integer) anInfo
                        .get(JmaParseRule.MAX_LENGTH);
                List<String> paths = (List<String>) anInfo
                        .get(JmaParseRule.PATHS);
                if (parameter == null || maxLength == null || paths == null) {
                    continue;
                }
                String message = "";
                for (String path : paths) {
                    String value = stringByXpath(xpath, path, doc);
                    if (StringUtils.isBlank(value) == false) {
                        if (message.length() == 0) {
                            message = value;
                        } else {
                            message = message + " " + value;
                        }
                    }
                }
                // メッセージを最大桁数で切り取り
                if (message.length() > maxLength) {
                    message = message.substring(0, maxLength);
                }

                if (StringUtils.isBlank(message) == false) {
                    // issueExtraに送信メッセージを格納
                    issueExtraMap.put(parameter, message);
                }
            }

            // 自動送信フラグの設定
            List<String> autoSendNumbers = rule.getAutoSendNumbers();
            if (autoSendNumbers.contains(disposition)) {
                isAutoSend = true;
            } else {
                isAutoSend = false;
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
                String remarksPath = (String) info
                        .get(JmaParseRule.REMARKS_PATH);
                String staticRemarks = (String) info
                        .get(JmaParseRule.STATICS_REMARKS);
                String allowType = (String) info.get(JmaParseRule.ALLOW_TYPE);

                log.finest("path -> " + path);
                log.finest("remarksPath ->" + remarksPath);
                log.finest("staticRemarks -> " + staticRemarks);
                log.finest("allowType -> " + allowType);

                // 地理情報のNode/text()を取得
                // この要素は複数ある可能性があるためNodeListで取得する
                NodeList nodes = nodelistByXpath(xpath, path, doc);
                // NodeListが取得できない場合は次のデータへ
                if (nodes == null) {
                    log.finest("paathでNodeのリストが取得できないため処理中断");
                    continue;
                }
                log.finest("pathで取得したNodeのリスト数 -> " + nodes.getLength());

                // 取得した全てのNodeに対して処理を行う
                for (int i = 0; i < nodes.getLength(); i++) {
                    // 1つのNodeを取得して処理
                    Node aNode = nodes.item(i);

                    // まずは該当Nodeを地理情報として使用できるかallowTypeを使用して確認
                    String type = stringByXpath(xpath, "../@type", aNode);  // Nodeの@typeを取得
                    if (StringUtils.isBlank(type) == false) {               // typeが設定されている場合は許可されたtypeか確認
                        if (StringUtils.isBlank(allowType) == false) {      // allowTypeの設定がない場合は全てOK
                            if (type.equals(allowType) == false) {          // type属性値が許可された値と異なるためこのデータはスキップ
                                continue;
                            }
                        }
                    }

                    // 次に地理情報を取得
                    String geoInfo = aNode.getNodeValue();

                    // 次に測地系を取得
                    String datum = stringByXpath(xpath, "../@datum", aNode);

                    // 次に関連する備考文字列を取得
                    // -----------------------------------------------------
                    // 備考用文字列取得
                    // parseRuleにREMARKS_PATHSが設定されている場合は配列に設定
                    // されているXpathを使用して備考文字列を取得する
                    // 設定されていない場合はSTATIC_REMARKSを使用して備考文字列を取得する
                    // -----------------------------------------------------
                    String remarks = null;
                    if (StringUtils.isBlank(staticRemarks) == false) {
                        // 固定文字を使用
                        remarks = staticRemarks;
                        log.finest("固定の備考文字列 -> " + remarks);
                    } else {
                        log.finest("固定の備考文字がないためXMLから備考文字を取得");
                        if (StringUtils.isBlank(remarksPath)) {
                            log.finest("備考文字取得用のパスが設定されていないため備考文字取得なし");
                        } else {
                            log.finest("備考文字取得用パス -> " + remarksPath);
                            remarks = stringByXpath(xpath, remarksPath, aNode);
                            log.finest("取得した備考文字列 -> " + remarks);
                        }
                    }

                    // 備考の最後に@typeで取得したデータ種類も追加する
                    if (StringUtils.isBlank(type) == false) {
                        if (StringUtils.isBlank(remarks)) {
                            remarks = type;
                        } else {
                            remarks = remarks + " " + type;
                        }
                    }

                    // Map型に格納してissueGeographyMapに追加
                    Map<String, String> map = new HashMap<String, String>();
                    String convertedGeoInfo = null;
                    // 位置情報をRest用に座標変換して格納
                    if (geoKey.equals("point")) {
                        convertedGeoInfo = StringUtils.convertPoint(geoInfo);
                    } else if (geoKey.equals("polygon")) {
                        convertedGeoInfo = StringUtils
                                .convertPointArray(geoInfo);
                    } else if (geoKey.equals("line")) {
                        convertedGeoInfo = StringUtils
                                .convertPointArray(geoInfo);
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

        log.finest("解析後の地理系情報 -> " + issueGeographyMaps);
    }

    /**
     * チケットの説明文に設定する文言を解析
     * 
     * @param doc
     * @param xpath
     * @param rule
     */
    private void parseDescription(Document doc, XPath xpath, JmaParseRule rule) {
        // 情報種別を取得
        String type = stringByXpath(xpath, rule.getDescriptionTypePath(), doc);
        Map<String, List<Map<String, String>>> contents = rule
                .getDescriptionContents();
        List<Map<String, String>> rules = contents.get(type);
        if (rules == null){
            return;
        }
        
        for (Map<String,String> aRuleMap : rules) {
            String path = aRuleMap.get("path");
            String value = stringByXpath(xpath, path, doc);
            if (value == null) {
                continue;
            }
            
        }
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

    public String getDisposition() {
        return disposition;
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

    public Map<String, String> getAutoSendExtras() {
        return autoSendExtras;
    }

}
