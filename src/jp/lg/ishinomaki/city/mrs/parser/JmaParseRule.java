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
    public static final String REMARKS_PATH = "remarks_path";
    public static final String STATICS_REMARKS = "static_remarks";
    public static final String COORDINATE = "coordinate";
    public static final String LINE = "line";
    public static final String POLYGON = "polygon";
    public static final String LOCATION = "Location";
    public static final String TRACKERS = "trackers";
    public static final String PROJECTS = "projects";
    public static final String AUTO_LAUNCH = "auto_launch";
    public static final String AUTO_SEND_DISPOSITION_NUMBERS = "auto_send_disposition_numbers";
    public static final String DISPOSITION = "disposition";
    public static final String NUMBER = "number";
    public static final String SEND_MESSAGE = "send_message";
    public static final String PARAMETER = "parameter";
    public static final String MAX_LENGTH = "max_length";
    public static final String DISPOSITION_NUMBERS = "disposition_numbers";
    public static final String PATHS = "paths";
    public static final String PATH = "path";
    public static final String TYPE = "type";
    public static final String EARTHQUAKE_THRESHOLD = "earthquake_threashold";
    public static final String TSUNAMI_THRESHOLD = "tsunami_threashold";
    public static final String EARTHQUAKE_PATH = "earthquake_path";
    public static final String TSUNAMI_PATH = "tsunami_path";
    public static final String LAUNCH = "launch";
    public static final String DEFAULT = "default";
    public static final String ALLOW_TYPE = "allow_type";
    public static final String AUTOSEND_EXTRAS = "autosend_extras";
    public static final String DESCRIPTION_TYPE_PATH = "description_type_path";
    public static final String DESCRIPTION_TARGET_PARAM = "description_target_param";
    public static final String DESCRIPTION_CONTENTS = "description_contents";

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
    private Map<String, Object> trackerInfo;

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
     * プロジェクト自動立ち上げを判断する震度を示す値へのXPath
     */
    private String autoLaunchSeismicIntensityXpath;

    /**
     * プロジェクト自動立ち上げを判断する津波の高さを示す値へのXPath
     */
    private String autoLaunchTsunamiHeightXpath;

    /**
     * プロジェクト自動立ち上げのための震度のしきい値
     */
    private String seismicIntensityThreashold;

    /**
     * プロジェクト自動立ち上げのための津波の高さのしきい値
     */
    private Double tsunamiHeightThreashold;

    /**
     * プロジェクト自動配信のための設定を格納したリスト
     */
    private List<Map<String, Object>> dispositionInfo;

    /**
     * プロジェクト自動配信用のメッセージ作成用設定を格納したリスト
     */
    private List<Map<String, Object>> sendMessageInfo;

    /**
     * プロジェクト自動送信を行うdispositionsのIDリスト
     */
    private List<String> autoSendNumbers;

    /**
     * プロジェクト自動送信を行う際の付随情報
     */
    private Map<String, String> autoSendExtras;

    /**
     * 説明文の種別を取得するためのXPath
     */
    private String descriptionTypePath;

    /**
     * 説明文を設定するRestAPIのパラメータ名
     */
    private String descriptionTargetParam;

    /**
     * 説明文に設定する内容を取得するためのXPath等情報
     */
    private Map<String, List<Map<String, String>>> descriptionContents;

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
     * 
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
            trackerInfo = (Map<String, Object>) rule.get(TRACKERS);

            // ------------------------------------------------
            // プロジェクト用定義
            // ------------------------------------------------
            Map<String, Object> project = (HashMap<String, Object>) rule
                    .get(PROJECTS);
            projectXpath = (String) project.get(PATH);

            // プロジェクト用テーブル取得
            projects = (Map<String, String>) project.get(TYPE);

            // デフォルトのプロジェクトID取得
            defaultProjectId = (String) project.get(DEFAULT);

            // ------------------------------------------------
            // プロジェクト自動立ち上げ用設定値取得
            // ------------------------------------------------
            Map<String, Object> autoLaunch = (HashMap<String, Object>) rule
                    .get(AUTO_LAUNCH);

            // 震度のXPath取得
            autoLaunchSeismicIntensityXpath = (String) autoLaunch
                    .get(EARTHQUAKE_PATH);
            // 津波のXPath取得
            autoLaunchTsunamiHeightXpath = (String) autoLaunch
                    .get(TSUNAMI_PATH);

            // 震度しきい値
            seismicIntensityThreashold = (String) autoLaunch
                    .get(EARTHQUAKE_THRESHOLD);
            // 津波高さしきい値
            tsunamiHeightThreashold = (Double) autoLaunch
                    .get(TSUNAMI_THRESHOLD);

            // ------------------------------------------------
            // プロジェクト自動配信用設定値取得
            // ------------------------------------------------
            dispositionInfo = (List<Map<String, Object>>) rule.get(DISPOSITION);

            // 送信メッセージ用設定値
            sendMessageInfo = (List<Map<String, Object>>) rule
                    .get(SEND_MESSAGE);

            // 自動送信対象の配備番号配列取得
            autoSendNumbers = (List<String>) rule
                    .get(AUTO_SEND_DISPOSITION_NUMBERS);

            // 自動送信時の付随情報
            autoSendExtras = (Map<String, String>) rule.get(AUTOSEND_EXTRAS);

            // ------------------------------------------------
            // 説明文取得用
            // ------------------------------------------------
            descriptionTypePath = (String) rule.get(DESCRIPTION_TYPE_PATH);
            descriptionTargetParam = (String) rule
                    .get(DESCRIPTION_TARGET_PARAM);
            descriptionContents = (Map<String, List<Map<String, String>>>) rule
                    .get(DESCRIPTION_CONTENTS);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Issues拡張カラムに設定するカラム名とXpathのテーブルを取得します
     * 
     * @return Issues拡張カラムに設定するキーと値を保持したテーブル
     */
    public Map<String, String> getIssueExtras() {
        return issueExtras;
    }

    /**
     * 情報タイプからトラッカーIDを取得します。<br>
     * 
     * @param infoType
     *            情報タイプ
     * @return String トラッカーID
     */
    @SuppressWarnings("unchecked")
    public String getTrackerId(String inputId, String infoType) {
        Map<String, Object> trackerInfoMap = (Map<String, Object>) trackerInfo
                .get(inputId);
        if (trackerInfoMap == null) {
            return null;
        }
        // デフォルトのトラッカーID取得
        String defaultTrackerId = (String) trackerInfoMap.get(DEFAULT);
        Map<String, String> trackers = (Map<String, String>) trackerInfoMap
                .get(TYPE);
        String trackerId = null;
        if (trackers != null) {
            trackerId = trackers.get(infoType);
        }
        if (trackerId == null) {
            // ルールから取得できなかった場合はデフォルトのトラッカーIDを使用する
            return defaultTrackerId;
        }
        return trackerId;
    }

    /**
     * 情報種別からプロジェクトIDを取得します。<br>
     * 
     * @return String プロジェクトID
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

    @SuppressWarnings("unchecked")
    public String getTrackerXpath(String inputId) {
        Map<String, Object> trackerInfoMap = (Map<String, Object>) trackerInfo
                .get(inputId);
        if (trackerInfoMap == null) {
            return null;
        }
        return (String) trackerInfoMap.get(PATH);
    }

    public String getAutoLaunchSeismicIntensityXpath() {
        return autoLaunchSeismicIntensityXpath;
    }

    public String getSeismicIntensityThreashold() {
        return seismicIntensityThreashold;
    }

    public String getAutoLaunchTsunamiHeightXpath() {
        return autoLaunchTsunamiHeightXpath;
    }

    public Double getTsunamiHeightThreashold() {
        return tsunamiHeightThreashold;
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

    public List<Map<String, Object>> getDispositionInfo() {
        return dispositionInfo;
    }

    public List<Map<String, Object>> getSendMessageInfo() {
        return sendMessageInfo;
    }

    public List<String> getAutoSendNumbers() {
        return autoSendNumbers;
    }

    public Map<String, String> getAutosendExtras() {
        return autoSendExtras;
    }

    public String getDescriptionTypePath() {
        return descriptionTypePath;
    }

    public String getDescriptionTargetParam() {
        return descriptionTargetParam;
    }

    public Map<String, List<Map<String, String>>> getDescriptionContents() {
        return descriptionContents;
    }
}
