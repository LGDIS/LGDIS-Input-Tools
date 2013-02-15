//
//  QueueConfig.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.queue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

import org.ho.yaml.Yaml;

/**
 * queue.ymlの各種定義内容を保持します
 */
public class QueueConfig {

    // 定義内容読み込み用識別子
    public static final String DOMAINSOCKET_DIR = "domainsocket_dir";
    public static final String DOMAINSOCKET_PUSH = "domainsocket_push_file";
    public static final String DOMAINSOCKET_POP = "domainsocket_pop_file";
    public static final String QUEUE_MAX_SIZE = "queue_max_size";

    private String domainSocketDir;
    private String domainSocketPushFile;
    private String domainSocketPopFile;
    private int queueMaxSize;

    /**
     * キューイング可能なデータサイズはintの最大値
     */
    public static final int DATA_MAX_SIZE = Integer.MAX_VALUE;

    /**
     * シングルトン設計
     */
    private static QueueConfig instance;

    /**
     * インスタンス取得メソッド
     * 
     * @return QueueConfig インスタンス
     */
    public static QueueConfig getInstance() {
        if (instance == null) {
            instance = new QueueConfig();
        }
        return instance;
    }

    /**
     * コンストラクタ.<br>
     * 
     */
    private QueueConfig() {
    }

    /**
     * 引数で指定されたymlファイルを読み込み、内容を保持する.<br>
     * 
     * @param ymlFile 読み込み対象のymlファイル
     */
    @SuppressWarnings("unchecked")
    public void loadYml(String ymlFile) throws FileNotFoundException {

        // 指定された定義ファイルの読み込み
        Map<String, Object> yml = (Map<String, Object>) Yaml
                .load(new FileReader(ymlFile));

        // --------------------------------------------------------
        // ドメインソケット用情報を読み込み
        // --------------------------------------------------------
        domainSocketDir = (String) yml.get(DOMAINSOCKET_DIR);
        domainSocketPushFile = (String) yml.get(DOMAINSOCKET_PUSH);
        domainSocketPopFile = (String) yml.get(DOMAINSOCKET_POP);

        // --------------------------------------------------------
        // キューに格納可能な最大値
        // --------------------------------------------------------
        queueMaxSize = (Integer) yml.get(QUEUE_MAX_SIZE);
    }

    public String getDomainSocketDir() {
        return domainSocketDir;
    }

    public String getDomainSocketPushFile() {
        return domainSocketPushFile;
    }

    public String getDomainSocketPopFile() {
        return domainSocketPopFile;
    }

    public int getQueueMaxSize() {
        return queueMaxSize;
    }

}
