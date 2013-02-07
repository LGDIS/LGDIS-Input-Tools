//
//  ParseRule.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.parser;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ho.yaml.Yaml;

/**
 * jma_parse_rule.ymlを読み込み各種設定値を取得するためのユーティリティクラスです
 * 
 */
public class JmaParseRule {

    /**
     * 定義体ロード用の識別子定義
     */
    public static final String XML_CONTROL = "xml_control";
    public static final String XML_HEAD = "xml_head";
    public static final String XML_BODY = "xml_body";
    public static final String ISSUE_EXTRAS = "issue_extras";
    public static final String ISSUE_GEOGRAPHIES = "issue_geographyies";
    public static final String REMARKS_PATHS = "remarks_paths";
    public static final String STATICS_REMARKS_PATH = "static_remark_path";
    public static final String COORDINATE = "coordinate";
    public static final String LINE = "line";
    public static final String POLYGON = "polygon";
    public static final String LOCATION = "Location";
    public static final String TRACKERS = "trackers";
    public static final String PROJECTS = "projects";
    public static final String AUTO = "auto";
    public static final String PATH = "path";
    public static final String TYPE = "type";
    public static final String EARTHQUAKE_THRESHOLD = "earthquake_threashold";
    public static final String TSUNAMI_THRESHOLD = "tsunami_threashold";
    public static final String EARTHQUAKE_PATH = "earthquake_path";
    public static final String TSUNAMI_PATH = "tsunami_path";
    public static final String LAUNCH = "launch";
    public static final String SEND = "send";
    public static final String DEFAULT = "default";
    public static final String ALLOW_TYPE = "allow_type";

    /**
     * yml定義内容を保持するテーブル
     */
    private HashMap<String, Object> rule;

    /**
     * xml_controlに設定する要素を取得するXPath
     */
    private String xmlControlPath;
    
    /**
     * xml_headに設定する要素を取得するXPath
     */
    private String xmlHeadPath;
    
    /**
     * xml_bodyに設定する要素を取得するXPath
     */
    private String xmlBodyPath;

    /**
     * key:issuesテーブルのフィールド名,value:対象のXpath
     */
    private Map<String, String> issueExtras;

    /**
     * Coordinateを取得するためのXpathのリスト
     */
    private List<Map<String, Object>> coordinateInfos;

    /**
     * Lineを取得するためのXpathのリスト
     */
    private List<Map<String, Object>> lineInfos;

    /**
     * Polygonを取得するためのXpathのリスト
     */
    private List<Map<String, Object>> polygonInfos;

    /**
     * Locationを取得するためのXpathのリスト
     */
    private List<Map<String, Object>> locationInfos;

    /**
     * key:Information type, value:トラッカーID
     */
    private Map<String, String> trackers;

    /**
     * トラッカーIDを引き当てるためのInfomationタグ->type属性へのXPath
     */
    private String trackerXpath;

    /**
     * デフォルトのトラッカーID
     */
    private String defaultTrackerId;
    
    /**
     * プロジェクトIDを引き当てるためのStatusタグへのXpath
     */
    private String projectXpath;

    /**
     * key:status, value:プロジェクトID
     */
    private Map<String, String> projects;

    /**
     * デフォルトのプロジェクトID
     */
    private String defaultProjectId;

    /**
     * 震度を示す値へのXPath
     */
    private String seismicIntensityXpath;

    /**
     * 津波の高さを示す値へのXPath
     */
    private String tsunamiHeightXpath;

    /**
     * プロジェクト自動立ち上げのための震度のしきい値
     */
    private String autoLaunchSeismicIntensityThreashold;

    /**
     * プロジェクト自動立ち上げのための津波の高さのしきい値
     */
    private Double autoLaunchTsunamiHeightThreashold;

    /**
     * プロジェクト自動配信のための震度のしきい値
     */
    private String autoSendSeismicIntensityThreashold;

    /**
     * プロジェクト自動配信のための津波の高さのしきい値
     */
    private Double autoSendTsunamiHeightThreashold;

    /**
     * シングルトンインスタンス
     */
    private static JmaParseRule instance;

    /**
     * インスタンス取得メソッド
     * 
     * @return
     */
    public static JmaParseRule getInstance() {
        if (instance == null) {
            instance = new JmaParseRule();
        }
        return instance;
    }

    /**
     * プライベートなコンストラクタ.<br>
     * インスタンス生成時にymlファイルを読み込み内容を取得します。
     */
    private JmaParseRule() {
        loadYml();
    }

    /**
     * yml定義ファイルを読み込み内容を取得するメソッド
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void loadYml() {
        try {
            // ymlファイルを読み込み、定義内容を解析
            // ymlファイルパスはアプリ構成定義から取得
            String ruleFilePath = ParserConfig.getInstance()
                    .getJmaRuleFilePath();
            Object obj = Yaml.load(new FileReader(ruleFilePath));

            // ymlの定義内容はMap形式であることが前提
            rule = (HashMap<String, Object>) obj;

            // xmlControlを取得するためのXPath
            xmlControlPath = (String) rule.get(XML_CONTROL);

            // xmlHeadを取得するためのXPath
            xmlHeadPath = (String) rule.get(XML_HEAD);

            // xmlBodyを取得するためのXPath
            xmlBodyPath = (String) rule.get(XML_BODY);

            // ------------------------------------------------
            // Issuesテーブル拡張フィールド用定義内容を保持
            // ------------------------------------------------
            issueExtras = (Map<String, String>) rule.get(ISSUE_EXTRAS);

            // ------------------------------------------------
            // issue_geographyies用定義内容を保持
            // それぞれの位置情報毎にXpathのリストを保持する
            // ------------------------------------------------
            Map issueGeoGrahyies = (Map<String, List<Object>>) rule
                    .get(ISSUE_GEOGRAPHIES);
            // Coordinateを取得するための情報リストを取得
            coordinateInfos = (List<Map<String, Object>>) issueGeoGrahyies
                    .get(COORDINATE);
            // Lineを取得するためのXpathリスト
            lineInfos = (List<Map<String, Object>>) issueGeoGrahyies.get(LINE);
            // Polygonを取得するためのXpathリスト
            polygonInfos = (List<Map<String, Object>>) issueGeoGrahyies
                    .get(POLYGON);
            // Locationを取得するためのXpathリスト
            locationInfos = (List<Map<String, Object>>) issueGeoGrahyies
                    .get(LOCATION);

            // ------------------------------------------------
            // トラッカー用定義
            // ------------------------------------------------
            // Information Type取得用のXPath
            Map<String, Object> tracker = (HashMap<String, Object>) rule
                    .get(TRACKERS);
            trackerXpath = (String) tracker.get(PATH);

            // トラッカー用テーブル取得
            trackers = (Map<String, String>) tracker.get(TYPE);

            // デフォルトのトラッカーID取得
            defaultTrackerId = (String)tracker.get(DEFAULT);
            
            // ------------------------------------------------
            // プロジェクト用定義
            // ------------------------------------------------
            Map<String, Object> project = (HashMap<String, Object>) rule
                    .get(PROJECTS);
            projectXpath = (String) project.get(PATH);

            // プロジェクト用テーブル取得
            projects = (Map<String, String>) project.get(TYPE);

            // デフォルトのプロジェクトID取得
            defaultProjectId = (String)project.get(DEFAULT);
            
            // ------------------------------------------------
            // プロジェクト自動立ち上げ/自動配信用設定値取得
            // ------------------------------------------------
            Map<String, Object> auto = (HashMap<String, Object>) rule.get(AUTO);

            // 震度のXPath取得
            seismicIntensityXpath = (String) auto.get(EARTHQUAKE_PATH);
            // 津波のXPath取得
            tsunamiHeightXpath = (String) auto.get(TSUNAMI_PATH);

            // 自動立ち上げ用のMap取得
            HashMap<String, Object> launchMap = (HashMap<String, Object>) auto
                    .get(LAUNCH);
            // 震度しきい値
            autoLaunchSeismicIntensityThreashold = (String) launchMap
                    .get(EARTHQUAKE_THRESHOLD);
            // 津波高さしきい値
            autoLaunchTsunamiHeightThreashold = (Double) launchMap
                    .get(TSUNAMI_THRESHOLD);

            // 自動配信用のMap取得
            HashMap<String, Object> sendMap = (HashMap<String, Object>) auto
                    .get(SEND);
            autoSendSeismicIntensityThreashold = (String) sendMap
                    .get(EARTHQUAKE_THRESHOLD);
            autoSendTsunamiHeightThreashold = (Double) sendMap
                    .get(TSUNAMI_THRESHOLD);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Issues拡張カラムに設定するカラム名とXpathのテーブルを取得します
     * 
     * @return
     */
    public Map<String, String> getIssueExtras() {
        return issueExtras;
    }

    /**
     * 情報タイプからトラッカーIDを取得します。<br>
     * 
     * @param infoType
     * @return
     */
    public String getTrackerId(String infoType) {
        String id = trackers.get(infoType);
        if (id == null) {
            // ルールから取得できなかった場合はデフォルトのトラッカーIDを使用する
            id = defaultTrackerId;
        }
        return id;
    }

    /**
     * 情報種別からプロジェクトIDを取得します。<br>
     * 
     * @return
     */
    public String getProjectId(String status) {
        String id = projects.get(status);
        if (id == null) {
            // ルールから取得できなかった場合はデフォルトのプロジェクトIDを使用する
            id = defaultProjectId;
        }
        return id;
    }

    public List<Map<String, Object>> getCoordinateInfos() {
        return coordinateInfos;
    }

    public List<Map<String, Object>> getLineInfos() {
        return lineInfos;
    }

    public List<Map<String, Object>> getPolygonInfos() {
        return polygonInfos;
    }

    public List<Map<String, Object>> getLocationInfos() {
        return locationInfos;
    }

    public String getTrackerXpath() {
        return trackerXpath;
    }

    public String getSeismicIntensityXpath() {
        return seismicIntensityXpath;
    }

    public String getAutoLaunchSeismicIntensityThreashold() {
        return autoLaunchSeismicIntensityThreashold;
    }

    public String getTsunamiHeightXpath() {
        return tsunamiHeightXpath;
    }

    public Double getAutoLaunchTsunamiHeightThreashold() {
        return autoLaunchTsunamiHeightThreashold;
    }

    public String getAutoSendSeismicIntensityThreashold() {
        return autoSendSeismicIntensityThreashold;
    }

    public Double getAutoSendTsunamiHeightThreashold() {
        return autoSendTsunamiHeightThreashold;
    }

    public Map<String, String> getTrackers() {
        return trackers;
    }

    public String getProjectXpath() {
        return projectXpath;
    }

    public Map<String, String> getProjects() {
        return projects;
    }

    public String getDefaultProjectId() {
        return defaultProjectId;
    }

    public String getXmlControlPath() {
        return xmlControlPath;
    }

    public String getXmlHeadPath() {
        return xmlHeadPath;
    }

    public String getXmlBodyPath() {
        return xmlBodyPath;
    }

    public String getDefaultTrackerId() {
        return defaultTrackerId;
    }

}
