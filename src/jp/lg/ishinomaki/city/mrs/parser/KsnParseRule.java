//
//  KsnParseRule.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.parser;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.ho.yaml.Yaml;

/**
 * parse_rule.ymlを読み込み各種設定値を取得するためのユーティリティクラスです
 * 
 */
public class KsnParseRule {

    /**
     * 定義体ロード用の識別子定義
     */
    public static final String XML_HEAD = "xml_head";
    public static final String XML_BODY = "xml_body";
    public static final String ISSUE_EXTRAS = "issue_extras";
    public static final String TRACKER = "tracker";
    public static final String PROJECTS = "projects";
    public static final String PATH = "path";
    public static final String TYPE = "type";
    public static final String DEFAULT = "default";

    /**
     * yml定義内容を保持するテーブル
     */
    private HashMap<String, Object> rule;

    /**
     * xmlHead部を取得するためのXPath
     */
    private String xmlHeadPath;

    /**
     * xmlBody部を取得するためのXPath
     */
    private String xmlBodyPath;

    /**
     * key:issuesテーブルのフィールド名,value:対象のXpath
     */
    private Map<String, String> issueExtras;

    /**
     * 河川のトラッカーID
     */
    private String trackerId;

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
     * シングルトンインスタンス
     */
    private static KsnParseRule instance;

    /**
     * インスタンス取得メソッド
     * 
     * @return
     */
    public static KsnParseRule getInstance() {
        if (instance == null) {
            instance = new KsnParseRule();
        }
        return instance;
    }

    /**
     * プライベートなコンストラクタ.<br>
     * インスタンス生成時にymlファイルを読み込み内容を取得します。
     */
    private KsnParseRule() {
        loadYml();
    }

    /**
     * yml定義ファイルを読み込み内容を取得するメソッド
     */
    @SuppressWarnings({ "unchecked" })
    public void loadYml() {
        try {
            // ymlファイルを読み込み、定義内容を解析
            // ymlファイルパスはアプリ構成定義から取得
            String ruleFilePath = ParserConfig.getInstance()
                    .getKsnRuleFilePath();
            Object obj = Yaml.load(new FileReader(ruleFilePath));

            // ymlの定義内容はMap形式であることが前提
            rule = (HashMap<String, Object>) obj;

            // xmlHead取得用のXpath
            xmlHeadPath = (String) rule.get(XML_HEAD);

            // xmlBody取得用のXpath
            xmlBodyPath = (String) rule.get(XML_BODY);

            // ------------------------------------------------
            // Issuesテーブル拡張フィールド用定義内容を保持
            // ------------------------------------------------
            issueExtras = (Map<String, String>) rule.get(ISSUE_EXTRAS);

            // ------------------------------------------------
            // トラッカー用定義
            // ------------------------------------------------
            // 河川用トラッカーIDを取得
            trackerId = (String) rule.get(TRACKER);

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

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getXmlHeadPath() {
        return xmlHeadPath;
    }

    public String getXmlBodyPath() {
        return xmlBodyPath;
    }

    public Map<String, String> getIssueExtras() {
        return issueExtras;
    }

    public String getTrackerId() {
        return trackerId;
    }

    /**
     * 情報種別からプロジェクトIDを取得します。<br>
     * 
     * @return
     */
    public String getProjectId(String status) {
        String id = projects.get(status);
        if (id == null) {
            // ルールから取得できなかった場合はstatusをIntegerに変換して再度取得
            try {
                Integer intStatus = Integer.parseInt(status);
                id = projects.get(intStatus);
            } catch (Exception e) {
            }
            if (id == null) {
                // ルールから取得できなかった場合はデフォルトのプロジェクトIDを使用する
                id = defaultProjectId;
            }
        }
        return id;
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
