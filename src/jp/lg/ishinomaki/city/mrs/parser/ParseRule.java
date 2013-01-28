//
//  ParseRule.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.parser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.lg.ishinomaki.city.mrs.AppConfig;

import org.ho.yaml.Yaml;

/**
 * parse_rule.ymlを読み込み各種設定値を取得するためのユーティリティクラスです
 * 
 */
public class ParseRule {

    /**
     * 定義体ロード用の識別子定義
     */
    private static final String ISSUES_EXTRAS = "issues_extras";
    private static final String ISSUES_ADDITION_DATUM_FIELDS = "issues_addition_datum_fields";
    private static final String CUSTOM_FIELDS = "custom_fields";
    private static final String TRACKERS = "trackers";
    private static final String PROJECTS = "projects";
    private static final String AUTO = "auto";
    private static final String PATH = "path";
    private static final String TYPE = "type";
    private static final String EARTHQUAKE_THRESHOLD = "earthquake_threashold";
    private static final String TSUNAMI_THRESHOLD = "tsunami_threashold";
    private static final String EARTHQUAKE_PATH = "earthquake_path";
    private static final String TSUNAMI_PATH = "tsunami_path";
    private static final String LAUNCH = "launch";
    private static final String SEND = "send";
    private static final String TARGETS = "targets";
    private static final String DEFAULT = "default";
    
    /**
     * yml定義内容を保持するテーブル
     */
    private HashMap<String, Object> rule;

    /**
     * key:issuesテーブルのフィールド名,value:対象のXpath
     */
    private Map<String, String> issuesExtras;
    
    /**
     * key:カスタムフィールドのID,value:対象のXpath
     */
    private Map<Integer, String> customFields;

    /**
     * key:Information type, value:トラッカーID
     */
    private Map<String, String> trackers;

    /**
     * トラッカーIDを引き当てるためのInfomationタグ->type属性へのXPath
     */
    private String trackerXpath;

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
    private Double autoLaunchSeismicIntensityThreashold;

    /**
     * プロジェクト自動立ち上げのための津波の高さのしきい値
     */
    private Double autoLaunchTsunamiHeightThreashold;

    /**
     * プロジェクト自動配信のための震度のしきい値
     */
    private Double autoSendSeismicIntensityThreashold;

    /**
     * プロジェクト自動配信のための津波の高さのしきい値
     */
    private Double autoSendTsunamiHeightThreashold;

    /**
     * プロジェクト自動配信時の配信先IDリスト
     */
    private ArrayList<String> autoSendTargets;

    /**
     * シングルトンインスタンス
     */
    private static ParseRule instance;

    /**
     * インスタンス取得メソッド
     * 
     * @return
     */
    public static ParseRule getInstance() {
        if (instance == null) {
            instance = new ParseRule();
        }
        return instance;
    }

    /**
     * プライベートなコンストラクタ.<br>
     * インスタンス生成時にymlファイルを読み込み内容を取得します。
     */
    private ParseRule() {
        loadYml();
    }

    /**
     * yml定義ファイルを読み込み内容を取得するメソッド
     */
    @SuppressWarnings("unchecked")
    public void loadYml() {
        try {
            // ymlファイルを読み込み、定義内容を解析
            // ymlファイルパスはアプリ構成定義から取得
            AppConfig appConfig = AppConfig.getInstance();
            String parse_rule_file = appConfig.getConfig("parse_rule_file");
            if (parse_rule_file == null) {
                parse_rule_file = "config/parse_rule.yml";
            }
            Object obj = Yaml.load(new FileReader(parse_rule_file));

            // ymlの定義内容はMap形式であることが前提
            rule = (HashMap<String, Object>) obj;

            // ------------------------------------------------
            // Issuesテーブル拡張フィールド用定義内容を保持
            // ------------------------------------------------
            issuesExtras = (Map<String, String>) rule.get(ISSUES_EXTRAS);
            
            // ------------------------------------------------
            // カスタムフィールド用定義内容を保持
            // ------------------------------------------------
            customFields = (Map<Integer, String>) rule.get(CUSTOM_FIELDS);

            // ------------------------------------------------
            // トラッカー用定義
            // ------------------------------------------------
            // Information Type取得用のXPath
            Map<String, Object> tracker = (HashMap<String, Object>) rule
                    .get(TRACKERS);
            trackerXpath = (String) tracker.get(PATH);

            // トラッカー用テーブル取得
            trackers = (Map<String, String>) tracker.get(TYPE);

            // ------------------------------------------------
            // プロジェクト用定義
            // ------------------------------------------------
            Map<String, Object> project = (HashMap<String, Object>) rule
                    .get(PROJECTS);
            projectXpath = (String) project.get(PATH);

            // プロジェクト用テーブル取得
            projects = (Map<String, String>) project.get(TYPE);

            // デフォルトのプロジェクトID取得
            defaultProjectId = projects.get(DEFAULT);
            
            // ------------------------------------------------
            // プロジェクト自動立ち上げ/自動配信用設定値取得
            // ------------------------------------------------
            Map<String, Object> auto = (HashMap<String, Object>) rule
                    .get(AUTO);

            // 震度のXPath取得
            seismicIntensityXpath = (String) auto.get(EARTHQUAKE_PATH);
            // 津波のXPath取得
            tsunamiHeightXpath = (String) auto.get(TSUNAMI_PATH);

            // 自動立ち上げ用のMap取得
            HashMap<String, Object> launchMap = (HashMap<String, Object>) auto
                    .get(LAUNCH);
            // 震度しきい値
            autoLaunchSeismicIntensityThreashold = (Double) launchMap
                    .get(EARTHQUAKE_THRESHOLD);
            // 津波高さしきい値
            autoLaunchTsunamiHeightThreashold = (Double) launchMap
                    .get(TSUNAMI_THRESHOLD);

            // 自動配信用のMap取得
            HashMap<String, Object> sendMap = (HashMap<String, Object>) auto
                    .get(SEND);
            autoSendSeismicIntensityThreashold = (Double) sendMap
                    .get(EARTHQUAKE_THRESHOLD);
            autoSendTsunamiHeightThreashold = (Double) sendMap
                    .get(TSUNAMI_THRESHOLD);

            // 配信先IDのリストを取得
            autoSendTargets = (ArrayList<String>) sendMap.get(TARGETS);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Issues拡張カラムに設定するカラム名とXpathのテーブルを取得します
     * @return
     */
    public Map<String, String> getIssuesExtras() {
        return issuesExtras;
    }
    
    /**
     * カスタムフィールドテーブルを取得します
     * 
     * @return
     */
    public Map<Integer, String> getCustomFields() {
        return customFields;
    }

    /**
     * 自動配信時の配信先リストを取得します
     * 
     * @return
     */
    public ArrayList<String> getAutoSendTargets() {
        return autoSendTargets;
    }

    /**
     * 情報タイプからトラッカーIDを取得します。
     * 
     * @param infoType
     * @return
     */
    public String getTrackerId(String infoType) {
        return trackers.get(infoType);
    }

    /**
     * 情報種別からプロジェクトIDを取得します。
     * 
     * @return
     */
    public String getProjectId(String status) {
        String id = projects.get(status);
        if (id == null) {
            id = defaultProjectId;
        }
        return id;
    }
    
    public String getTrackerXpath() {
        return trackerXpath;
    }

    public String getSeismicIntensityXpath() {
        return seismicIntensityXpath;
    }

    public Double getAutoLaunchSeismicIntensityThreashold() {
        return autoLaunchSeismicIntensityThreashold;
    }

    public String getTsunamiHeightXpath() {
        return tsunamiHeightXpath;
    }

    public Double getAutoLaunchTsunamiHeightThreashold() {
        return autoLaunchTsunamiHeightThreashold;
    }

    public Double getAutoSendSeismicIntensityThreashold() {
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

}
