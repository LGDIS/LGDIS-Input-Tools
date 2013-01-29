package jp.lg.ishinomaki.city.mrs.receiver;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

import org.ho.yaml.Yaml;

/**
 * receriver.ymlの内容を保持します
 *
 */
public class ReceiverConfig {

    public static final String THREADS = "threads";
    public static final String IP = "ip";
    public static final String PORT = "port";
    public static final String OUTPUT = "output";
    public static final String ANALYZERS = "analyzers";
    public static final String INPUT_ID = "input_id";
    public static final String BCH_DIVIDER = "bch_divider";

    
    private Map<String, Object> threads;
    
    private List<Map<String, Integer>> bch_divider;
    
    /**
     * シングルトン設計
     */
    private static ReceiverConfig instance;

    public static ReceiverConfig getInstance() {
        if (instance == null) {
            instance = new ReceiverConfig();
        }
        return instance;
    }

    private ReceiverConfig() {
    }

    /**
     * ymlファイル読み込み
     */
    @SuppressWarnings("unchecked")
    public void loadYml(String ymlFile) throws FileNotFoundException {
        
        // 指定された定義ファイルの読み込み
        Map<String, Object> yml = (Map<String, Object>)Yaml.load(new FileReader(ymlFile));
        
        // --------------------------------------------------------
        // スレッド情報を読み込み
        // --------------------------------------------------------
        threads = (Map<String, Object>)yml.get(THREADS);
        
        // --------------------------------------------------------
        // BCH情報を読み込み
        // --------------------------------------------------------
        bch_divider = (List<Map<String, Integer>>) yml.get(BCH_DIVIDER);
    }

    public Map<String, Object> getThreads() {
        return threads;
    }

    public List<Map<String, Integer>> getBch_divider() {
        return bch_divider;
    }
    
    
}
