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
    public static final String PROJECT = "project";

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
     * 河川のプロジェクトID
     */
    private String projectId;

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
            // 河川用プロジェクトID
            projectId = (String) rule.get(PROJECT);

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

    public String getProjectId() {
        return projectId;
    }

}
