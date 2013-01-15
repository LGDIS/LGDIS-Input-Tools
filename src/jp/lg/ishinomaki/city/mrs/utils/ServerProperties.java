//
//  JmaServerProperties.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.utils;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * JMA受信サーバアプリのプロパティファイル内容を保管します。<br>
 * コンストラクタでプロパティファイル内容をロードします。<br>
 * プロパティの形式が不正な場合は例外をスローします。形式不備は設定ミスのため直ちにアプリケーションを終了するようにしてください。<br>
 * 
 */
public class ServerProperties {

    /**
     * プロパティファイルに定義される1スレッド毎の定義種類の数
     */
    private static int PROPERTY_DEFINE_NUM = 4;

    /**
     * スレッド名をリストで保管
     */
    private List<String> threadNames = new ArrayList<String>();

    /**
     * スレッド名をキーにソケット接続に必要な各種情報を保管
     */
    private Map<String, Map<String, String>> threadInfos = new HashMap<String, Map<String, String>>();

    /**
     * コンストラクタです。 ファイルの読み込みで失敗した場合はIOExceptionをスローします。
     * プロパティファイルの内容が不正な場合はExceptionをスローします。
     * 
     * @param filePath
     *            プロパティファイルパス
     * @throws Exception
     *             何かがおかしければすべて例外とする
     */
    public ServerProperties(String filePath) throws Exception {

        // プロパティファイル読み込み
        Properties prop = new Properties();
        prop.load(new FileReader(filePath));

        // threadsをリストで保持
        String strThreads = prop.getProperty("threads");
        if (strThreads == null) {
            throw new Exception("no exist 'threads' in ["
                    + filePath + "].");
        }
        // threadsをカンマ区切りでListに保持
        StringTokenizer st = new StringTokenizer(strThreads, ",");
        while (st.hasMoreTokens()) {
            // スレッド名を1つづつ処理
            String threadName = st.nextToken();
            // スレッド名の空文字は不可
            if (threadName.length() == 0) {
                throw new Exception("invalid thread-name in [" + filePath
                        + "].");
            }
            // スレッド名の重複は不可
            if (this.threadNames.contains(threadName)) {
                throw new Exception("duplicated thread-name in [" + filePath
                        + "].");
            }
            // リストに保存
            this.threadNames.add(threadName);

            // 該当のスレッド名に対する定義をthreadInfosに保存
            Map<String, String> threadInfo = new HashMap<String, String>();
            for (Object aKey : prop.keySet().toArray()) {
                String propertyKey = (String) aKey;

                // 現在処理中のスレッド名に対する定義
                if (propertyKey.startsWith(threadName)) {

                    // キー名称の正当性チェック
                    if (propertyKey.equals(threadName + ".port")
                            || propertyKey.equals(threadName + ".ip")
                            || propertyKey.equals(threadName + ".analyzer")
                            || propertyKey.equals(threadName + ".output")) {
                        String propertyValue = prop.getProperty((String) aKey);
                        threadInfo.put((String) aKey, propertyValue);
                    } else {
                        throw new Exception("invalid definition thread-name:"
                                + threadName + "in [" + filePath + "].");
                    }
                }
            }

            // 設定要素数は5つになるはず。そうでない場合は設定エラー。
            if (threadInfo.size() != PROPERTY_DEFINE_NUM) {
                throw new Exception("invalid definition thread-name:"
                        + threadName + "in [" + filePath + "].");
            }

            // 定義保存
            this.threadInfos.put(threadName, threadInfo);
        }

    }

    /**
     * プロパティファイルに定義されているスレッド名のリストを取得します。
     * 
     * @return List プロパティファイルリスト
     */
    public List<String> getThreadNames() {
        return this.threadNames;
    }

    /**
     * 引数で指定したスレッド名に対してプロパティファイルに定義されているIPアドレス("ip")を取得します。
     * 
     * @param threadName
     *            スレッド名
     * @return String IPアドレスの文字列表現
     */
    public String getIpAddressFromThreadName(String threadName) {
        Map<String, String> threadInfo = this.threadInfos.get(threadName);
        return threadInfo.get(threadName + ".ip");

    }

    /**
     * 引数で指定したスレッド名に対してプロパティファイルに定義されているポート番号("port")を取得します。
     * 
     * @param threadName
     *            スレッド名
     * @return int ポート番号
     */
    public int getPortNoFromThreadName(String threadName) {
        Map<String, String> threadInfo = this.threadInfos.get(threadName);
        String strPortNo = threadInfo.get(threadName + ".port");
        return Integer.valueOf(strPortNo);
    }

    /**
     * 引数で指定したスレッド名に対してプロパティファイルに定義されているデータ解析クラス("analyzer")を取得します。
     * 
     * @param threadName
     * @return
     */
    public String getAnalyzerFromThreadName(String threadName) {
        Map<String, String> threadInfo = this.threadInfos.get(threadName);
        return threadInfo.get(threadName + ".analyzer");
    }

    /**
     * 引数で指定したスレッド名に対してプロパティファイルに定義されているXMLファイル出力先ディレクトリ("output")を取得します。
     * 
     * @param threadName
     * @return
     */
    public String getOutputPathFromThreadName(String threadName) {
        Map<String, String> threadInfo = this.threadInfos.get(threadName);
        String outputPath = threadInfo.get(threadName + ".output");
        return outputPath;
    }

}
