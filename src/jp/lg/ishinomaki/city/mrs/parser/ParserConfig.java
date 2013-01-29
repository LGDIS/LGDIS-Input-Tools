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

    public static final String REDMINE = "redmine";
    public static final String PROTOCOL = "protocol";
    public static final String TARGET_HOST = "target_host";
    public static final String TARGET_PORT = "target_port";
    public static final String POST_API = "post_api";
    public static final String API_KEY = "api_key";
    public static final String TIMEOUT = "timeout";
    public static final String RETRY_COUNT = "retry_count";
    public static final String BASICAUTH_ID = "basicauth_id";
    public static final String BASICAUTH_PASSWORD = "basicauth_password";
    public static final String RULE_FILE = "rule_file";
    public static final String SCHEMA_FILE = "schema_file";

    private Map<String, Object> redmine;

    private String ruleFilePath;

    private String schemaFilePath;

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
        ruleFilePath = (String) yml.get(RULE_FILE);

        // --------------------------------------------------------
        // スキーマファイルパスを読み込み
        // --------------------------------------------------------
        schemaFilePath = (String) yml.get(SCHEMA_FILE);
    }

    public Map<String, Object> getRedmine() {
        return redmine;
    }

    public String getRuleFilePath() {
        return ruleFilePath;
    }

    public String getSchemaFilePath() {
        return schemaFilePath;
    }

}
