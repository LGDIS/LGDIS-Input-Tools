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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import jp.lg.ishinomaki.city.mrs.utils.DateUtils;
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
     * プロジェクト自動立ち上げフラグを送信した最終時刻を保存
     */
    public static Date lastDateTimeAutoLaunch;

    /**
     * プロジェクト自動送信フラグを送信した最終時刻を保存
     */
    public static Date lastDateTimeAutoSend;

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
            // 前回自動立ち上げからの間隔が設定値より大きい場合のみ実施
            parseIsAutoLaunchBySeismicIntensity(doc, xpath, rule);
            parseIsAutoLaunchByTsunamiHeigh(doc, xpath, rule);

            // TODO プロジェクト自動配信の判定は仕様変更の可能性があるため保留

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
                log.finest("震度:" + intensity + " しきい値:" + threshold);
                // さらに前回自動立ち上げからの時間経過チェック
                if (isAutoLaunchByInterval(rule)) {
                    log.info("プロジェクト自動立ち上げを指示します。震度:" + intensity + " しきい値:"
                            + threshold);
                    isAutoLaunch = true;
                }
                break;
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
                    log.finest("高さ:" + sHeight + " しきい値:"
                            + autoLaunchTsunamiHeightThreshold);
                    // さらに前回自動立ち上げからの時間経過チェック
                    if (isAutoLaunchByInterval(rule)) {
                        log.info("プロジェクト自動立ち上げを指示します。高さ:" + sHeight + " しきい値:"
                                + autoLaunchTsunamiHeightThreshold);
                        isAutoLaunch = true;
                    }
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
                log.finest("震度:" + intensity + " しきい値:" + threshold);
                // さらに前回自動送信からの時間経過チェック
                if (isAutoSendByInterval(rule)) {
                    isAutoSend = true;
                    log.info("プロジェクト自動送信を指示します。震度:" + intensity + " しきい値:"
                            + threshold);
                }
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
    boolean paseIsAutoSendByTsunamiHeight(Document doc, XPath xpath,
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
                    log.finest("高さ:" + sHeight + " しきい値:"
                            + autoSendTsunamiHeightThreshold);
                    // さらに前回自動送信からの時間経過チェック
                    if (isAutoSendByInterval(rule)) {
                        isAutoSend = true;
                        log.info("プロジェクト自動送信を指示します。高さ:" + sHeight + " しきい値:"
                                + autoSendTsunamiHeightThreshold);
                    }
                    break;
                }
            }
        }
        return true;
    }

    /**
     * 設定された時間間隔(分単位)によりプロジェクト自動立ち上げを行なってよいかを判定します。
     * 
     * @return
     */
    boolean isAutoLaunchByInterval(JmaParseRule rule) {
        int interval = rule.getAutoLaunchInterval();
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
     * @return
     */
    boolean isAutoSendByInterval(JmaParseRule rule) {
        int interval = rule.getAutoSendInterval();
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
