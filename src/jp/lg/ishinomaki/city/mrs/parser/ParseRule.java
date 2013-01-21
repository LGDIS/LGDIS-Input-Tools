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

import jp.lg.ishinomaki.city.mrs.AppConfig;

import org.ho.yaml.Yaml;

/**
 * parse_rule.ymlを読み込み各種設定値を取得するためのユーティリティクラスです
 * 
 */
public class ParseRule {

    /**
     * yml定義内容を保持するテーブル
     */
    private HashMap<String, Object> rule;

    private String statusXpath;

    private String editorialOfficeXpath;

    private String publishingOfficeXpath;

    private String reportDateTimeXpath;

    private String targetDateTimeXpath;

    private String targetDtDubiousXpath;

    private String targetDurationXpath;

    private String validDateTimeXpath;

    private String eventIdXpath;

    private String infoTypeXpath;

    private String serialXpath;

    private String infoKindXpath;

    private String infoKindVersionXpath;

    private String textXpath;

    private String causeXpath;

    private String applyXpath;

    private String informationTypeXpath;

    private String jmaProjectId;

    private String kasenProjectId;

    private String trainingProjectId;
    
    private String testProjectId;
    
    private String autoLaunchSeismicIntensityXpath;

    private Double autoLaunchSeismicIntensityThreashold;

    private String autoLaunchTsunamiHeightXpath;

    private Double autoLaunchTsunamiHeightThreashold;

    private String autoSendSeismicIntensityXpath;

    private Double autoSendSeismicIntensityThreashold;

    private ArrayList<String> autoSendTsunamiHeightXpaths;

    private Double autoSendTsunamiHeightThreashold;

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
            Object obj = Yaml.load(new FileReader(parse_rule_file));

            // ymlの定義内容はMap形式であることが前提
            rule = (HashMap<String, Object>) obj;

            // ------------------------------------------------
            // 各種Xpathの文字列を取得する
            // ------------------------------------------------
            HashMap<String, String> customField = (HashMap<String, String>) rule
                    .get("カスタムフィールド");

            // Status
            statusXpath = customField.get("Status");
            // EditorialOffice
            editorialOfficeXpath = customField.get("EditorialOffice");
            // PublishingOffice
            publishingOfficeXpath = customField.get("PublishingOffice");
            // ReportDateTime
            reportDateTimeXpath = customField.get("ReportDateTime");
            // TargetDateTime
            targetDateTimeXpath = customField.get("TargetDateTime");
            // TargetDtDubious
            targetDtDubiousXpath = customField.get("TargetDtDubious");
            // TargetDuration
            targetDurationXpath = customField.get("TargetDuration");
            // ValidDateTime
            validDateTimeXpath = customField.get("ValidDateTime");
            // EventID
            eventIdXpath = customField.get("EventID");
            // InfoType
            infoTypeXpath = customField.get("InfoType");
            // Serial
            serialXpath = customField.get("Serial");
            // InfoKind
            infoKindXpath = customField.get("InfoKind");
            // InfoKindVersion
            infoKindVersionXpath = customField.get("InfoKindVersion");
            // Text
            textXpath = customField.get("Text");
            // Cause
            causeXpath = customField.get("Cause");
            // Apply
            applyXpath = customField.get("Apply");

            // Information Type取得パス
            HashMap<String, Object> tracker = (HashMap<String, Object>) rule
                    .get("トラッカー");
            informationTypeXpath = (String) tracker.get("Path");

            // ------------------------------------------------
            // プロジェクト自動立ち上げ用設定値取得
            // ------------------------------------------------
            HashMap<String, Object> autoLaunch = (HashMap<String, Object>) rule
                    .get("プロジェクト自動立ち上げ");
            // 地震用
            HashMap<String, Object> autoLaunchEarthquake = (HashMap<String, Object>) autoLaunch
                    .get("地震");
            autoLaunchSeismicIntensityXpath = (String) autoLaunchEarthquake
                    .get("Path");
            autoLaunchSeismicIntensityThreashold = (Double) autoLaunchEarthquake
                    .get("震度");

            // 津波用
            HashMap<String, Object> autoLaunchTsunami = (HashMap<String, Object>) autoLaunch
                    .get("津波");
            autoLaunchTsunamiHeightXpath = (String) autoLaunchTsunami
                    .get("Path");
            autoLaunchTsunamiHeightThreashold = (Double) autoLaunchTsunami
                    .get("高さ");

            // ------------------------------------------------
            // プロジェクト自動配信用設定値取得
            // ------------------------------------------------
            HashMap<String, Object> autoSend = (HashMap<String, Object>) rule
                    .get("プロジェクト自動配信");
            // 地震用
            HashMap<String, Object> autoSendEarthquake = (HashMap<String, Object>) autoSend
                    .get("地震");
            autoSendSeismicIntensityXpath = (String) autoSendEarthquake
                    .get("Path");
            autoSendSeismicIntensityThreashold = (Double) autoSendEarthquake
                    .get("震度");

            // 津波用
            HashMap<String, Object> autoSendTsunami = (HashMap<String, Object>) autoSend
                    .get("津波");
            autoSendTsunamiHeightXpaths = (ArrayList<String>) autoSendTsunami
                    .get("Path");
            autoSendTsunamiHeightThreashold = (Double) autoSendEarthquake
                    .get("高さ");

            // ------------------------------------------------
            // 固定プロジェクトID取得
            // ------------------------------------------------
            HashMap<String, String> project = (HashMap<String, String>) rule
                    .get("プロジェクト");
            jmaProjectId = project.get("JMAプロジェクトID");
            kasenProjectId = project.get("河川プロジェクトID");
            trainingProjectId = project.get("訓練プロジェクトID");
            testProjectId = project.get("通信テストプロジェクトID");
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getStatusXpath() {
        return statusXpath;
    }

    public String getEditorialOfficeXpath() {
        return editorialOfficeXpath;
    }

    public String getPublishingOfficeXpath() {
        return publishingOfficeXpath;
    }

    public String getReportDateTimeXpath() {
        return reportDateTimeXpath;
    }

    public String getTargetDateTimeXpath() {
        return targetDateTimeXpath;
    }

    public String getTargetDtDubiousXpath() {
        return targetDtDubiousXpath;
    }

    public String getTargetDurationXpath() {
        return targetDurationXpath;
    }

    public String getValidDateTimeXpath() {
        return validDateTimeXpath;
    }

    public String getEventIdXpath() {
        return eventIdXpath;
    }

    public String getInfoTypeXpath() {
        return infoTypeXpath;
    }

    public String getSerialXpath() {
        return serialXpath;
    }

    public String getInfoKindXpath() {
        return infoKindXpath;
    }

    public String getInfoKindVersionXpath() {
        return infoKindVersionXpath;
    }

    public String getTextXpath() {
        return textXpath;
    }

    public String getCauseXpath() {
        return causeXpath;
    }

    public String getApplyXpath() {
        return applyXpath;
    }

    public String getInformationTypeXpath() {
        return informationTypeXpath;
    }

    public String getJmaProjectId() {
        return jmaProjectId;
    }

    public String getKasenProjectId() {
        return kasenProjectId;
    }

    public String getAutoLaunchSeismicIntensityXpath() {
        return autoLaunchSeismicIntensityXpath;
    }

    public Double getAutoLaunchSeismicIntensityThreashold() {
        return autoLaunchSeismicIntensityThreashold;
    }

    public String getAutoLaunchTsunamiHeightXpath() {
        return autoLaunchTsunamiHeightXpath;
    }

    public Double getAutoLaunchTsunamiHeightThreashold() {
        return autoLaunchTsunamiHeightThreashold;
    }

    public String getAutoSendSeismicIntensityXpath() {
        return autoSendSeismicIntensityXpath;
    }

    public Double getAutoSendSeismicIntensityThreashold() {
        return autoSendSeismicIntensityThreashold;
    }

    public ArrayList<String> getAutoSendTsunamiHeightXpaths() {
        return autoSendTsunamiHeightXpaths;
    }

    public Double getAutoSendTsunamiHeightThreashold() {
        return autoSendTsunamiHeightThreashold;
    }

    public String getTrainingProjectId() {
        return trainingProjectId;
    }

    public String getTestProjectId() {
        return testProjectId;
    }

    /**
     * 情報タイプからトラッカーを取得します
     * 
     * @param infoType
     * @return
     */
    @SuppressWarnings("unchecked")
    public String getTracker(String infoType) {

        HashMap<String, Object> tracker = (HashMap<String, Object>) rule
                .get("トラッカー");
        ArrayList<HashMap<String, String>> types = (ArrayList<HashMap<String, String>>) tracker
                .get("Type");

        // 全てのTypeを確認しヒットするものがあればそれに紐づくトラッカーIDを返却
        for (HashMap<String, String> type : types) {
            String trackerId = type.get(infoType);
            if (trackerId != null) {
                return trackerId;
            }
        }
        return null;
    }
}
