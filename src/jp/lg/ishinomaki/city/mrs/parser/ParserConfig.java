//
//  ParserConfig.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

import org.ho.yaml.Yaml;

/**
 * parser.ymlの内容を保持します
 * 
 */
public class ParserConfig {

    // -------------------------------------------------------
    // 設定ファイルから値を取得する際の識別子定義
    // -------------------------------------------------------
    public static final String REDMINE = "redmine";
    public static final String PROTOCOL = "protocol";
    public static final String TARGET_HOST = "target_host";
    public static final String TARGET_PORT = "target_port";
    public static final String ISSUES_POST_API = "issues_post_api";
    public static final String UPLOADS_POST_API = "uploads_post_api";
    public static final String ISSUES_POST_CONTENT_TYPE = "issues_post_content_type";
    public static final String UPLOADS_POST_CONTENT_TYPE = "uploads_post_content_type";
    public static final String API_KEY = "api_key";
    public static final String TIMEOUT = "timeout";
    public static final String RETRY_COUNT = "retry_count";
    public static final String BASICAUTH_ID = "basicauth_id";
    public static final String BASICAUTH_PASSWORD = "basicauth_password";
    public static final String JMA_RULE_FILE = "jma_rule_file";
    public static final String KSN_RULE_FILE = "ksn_rule_file";
    public static final String JMA_SCHEMA_FILE = "jma_schema_file";
    public static final String KSN_SCHEMA_FILE = "ksn_schema_file";
    public static final String TAR_ATTACHMENT_STATICS = "tar_attachment_statics";
    public static final String PDF_ATTACHMENT_STATICS = "pdf_attachment_statics";
    public static final String TEXT_ATTACHMENT_STATICS = "text_attachment_statics";
    public static final String SUBJECT = "subject";
    public static final String PROJECT_ID = "project_id";
    public static final String TRAINING_PROJECT_ID = "training_project_id";
    public static final String TEST_PROJECT_ID = "test_project_id";
    public static final String TRACKER_ID = "tracker_id";
    public static final String FILENAME = "filename";
    public static final String DESCRIPTION = "description";
    public static final String CONTENT_TYPE = "content_type";

    /**
     * Redmine用設定情報
     */
    private Map<String, Object> redmine;

    /**
     * JMA用のXML解析ルール定義ファイルのパス
     */
    private String jmaRuleFilePath;

    /**
     * 河川用のXML解析ルール定義ファイルのパス
     */
    private String ksnRuleFilePath;

    /**
     * JMAXML解析スキーマファイルのパス
     */
    private String jmaSchemaFilePath;

    /**
     * 河川XML解析スキーマファイルのパス
     */
    private String ksnSchemaFilePath;

    /**
     * PDF添付の用チケット情報
     */
    private Map<String, String> pdfAttachmentStatics;

    /**
     * Text添付チケット用の情報
     */
    private Map<String, String> textAttachmentStatics;

    /**
     * Tar添付チケット用の情報
     */
    private Map<String, String> tarAttachmentStatics;

    /**
     * 訓練用の固定プロジェクトID
     */
    private String trainingProjectId;

    /**
     * 通信試験用の固定プロジェクトID
     */
    private String testProjectId;

    /**
     * シングルトン設計
     */
    private static ParserConfig instance;

    public static ParserConfig getInstance() {
        if (instance == null) {
            instance = new ParserConfig();
        }
        return instance;
    }

    private ParserConfig() {
    }

    /**
     * ymlファイル読み込み
     */
    @SuppressWarnings("unchecked")
    public void loadYml(String ymlFile) throws FileNotFoundException {

        // 指定された定義ファイルの読み込み
        Map<String, Object> yml = (Map<String, Object>) Yaml
                .load(new FileReader(ymlFile));

        // --------------------------------------------------------
        // Redmine送信用情報を読み込み
        // --------------------------------------------------------
        redmine = (Map<String, Object>) yml.get(REDMINE);

        // --------------------------------------------------------
        // 解析ルールファイルパスを読み込み
        // --------------------------------------------------------
        jmaRuleFilePath = (String) yml.get(JMA_RULE_FILE);
        ksnRuleFilePath = (String) yml.get(KSN_RULE_FILE);

        // --------------------------------------------------------
        // スキーマファイルパスを読み込み
        // --------------------------------------------------------
        jmaSchemaFilePath = (String) yml.get(JMA_SCHEMA_FILE);
        ksnSchemaFilePath = (String) yml.get(KSN_SCHEMA_FILE);

        // --------------------------------------------------------
        // PDF添付チケット用の情報
        // --------------------------------------------------------
        pdfAttachmentStatics = (Map<String, String>) yml
                .get(PDF_ATTACHMENT_STATICS);

        // --------------------------------------------------------
        // TextF添付チケット用の情報
        // --------------------------------------------------------
        textAttachmentStatics = (Map<String, String>) yml
                .get(TEXT_ATTACHMENT_STATICS);

        // --------------------------------------------------------
        // TextF添付チケット用の情報
        // --------------------------------------------------------
        tarAttachmentStatics = (Map<String, String>) yml
                .get(TAR_ATTACHMENT_STATICS);

        // --------------------------------------------------------
        // 固定プロジェクトID取得
        // --------------------------------------------------------
        trainingProjectId = (String) yml.get(TRAINING_PROJECT_ID);
        testProjectId = (String) yml.get(TEST_PROJECT_ID);
    }

    public Map<String, Object> getRedmine() {
        return redmine;
    }

    public String getJmaRuleFilePath() {
        return jmaRuleFilePath;
    }

    public String getKsnRuleFilePath() {
        return ksnRuleFilePath;
    }

    public String getJmaSchemaFilePath() {
        return jmaSchemaFilePath;
    }

    public String getKsnSchemaFilePath() {
        return ksnSchemaFilePath;
    }

    public Map<String, String> getPdfAttachmentStatics() {
        return pdfAttachmentStatics;
    }

    public Map<String, String> getTextAttachmentStatics() {
        return textAttachmentStatics;
    }

    public Map<String, String> getTarAttachmentStatics() {
        return tarAttachmentStatics;
    }

    public String getTrainingProjectId() {
        return trainingProjectId;
    }

    public String getTestProjectId() {
        return testProjectId;
    }

}
