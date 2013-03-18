//
//  ReceiverThreadManager.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.receiver;

import java.util.HashMap;
import java.util.Map;

import jp.lg.ishinomaki.city.mrs.analyzer.DataAnalyzer;
import jp.lg.ishinomaki.city.mrs.receiver.jma.JmaServerSocketControl;

/**
 * スレッド管理クラス
 * 
 */
public class ReceiverThreadManager {

    /**
     * JmaServerThreadインスタンスを保持するテーブル キー:スレッド名 値:JmaServerThreadインスタンス
     */
    Map<String, ReceiverThread> threadManager = null;

    /**
     * コンストラクタです。<br>
     * mainからコールされ実際のメッセージ受信アプリケーション起動処理を行います。<br>
     * プロパティファイルをロードし定義されているスレッドを起動します。<br>
     * その後の処理はすべてスレッドクラスに任せます。<br>
     * プロパティロード〜スレッド起動時に異常が発生した場合は直ちにアプリケーションを停止します。
     */
    @SuppressWarnings("unchecked")
    public ReceiverThreadManager() throws Exception {

        // プロパティファイルのルールに従って各スレッド起動
        // スレッド起動
        threadManager = new HashMap<String, ReceiverThread>(); // スレッド管理テーブル生成

        // スレッド定義情報を取得
        Map<String, Object> threads = ReceiverConfig.getInstance().getThreads();
        for (String threadName : threads.keySet()) {
            // スレッド名からプロパティの各種設定を取得
            Map<String, Object> threadInfo = (Map<String, Object>) threads
                    .get(threadName);
            // IPアドレス
            String ipAddress = (String) threadInfo.get(ReceiverConfig.IP);

            // ポート番号
            int portNo = Integer.parseInt((String) threadInfo
                    .get(ReceiverConfig.PORT));

            // ファイル出力先
            String outputPath = (String) threadInfo.get(ReceiverConfig.OUTPUT);

            // ソケット制御生成
            JmaServerSocketControl socketControl = new JmaServerSocketControl(
                    ipAddress, portNo);

            // 入力元識別子
            String inputId = (String) threadInfo.get(ReceiverConfig.INPUT_ID);

            // モード
            Integer mode = (Integer) threadInfo.get(ReceiverConfig.MODE);

            // DataAnalyzerクラス名
            // 複数あり、JMAデータタイプ毎に定義されている
            Map<String, DataAnalyzer> analyzers = new HashMap<String, DataAnalyzer>();
            Map<String, String> analyzerMap = (Map<String, String>) threadInfo
                    .get(ReceiverConfig.ANALYZERS);
            for (String dataType : analyzerMap.keySet()) {
                String className = analyzerMap.get(dataType);
                DataAnalyzer analyzer = createAnalyzer(className);
                if (analyzer != null) {
                    // dataTypeを大文字に変換
                    dataType = dataType.toUpperCase();
                    analyzers.put(dataType, analyzer);
                }
            }

            // スレッド生成
            ReceiverThread thread = new ReceiverThread(threadName,
                    socketControl, outputPath, analyzers, inputId, mode);

            // スレッド管理テーブルに登録
            threadManager.put(threadName, thread);
            // ここではスレッドインスタンスを作成するだけで起動しない！
        }
    }

    /**
     * スレッド開始
     */
    public void start() {
        // スレッド管理テーブルに登録されている全てのスレッドを実行
        for (ReceiverThread thread : threadManager.values()) {
            thread.start();
        }
    }

    /**
     * スレッド停止
     */
    public void stop() {
        // スレッド管理テーブルに登録されているすべてのスレッドを停止
        for (ReceiverThread thread : threadManager.values()) {
            thread.done();
        }
    }

    /**
     * 内部メソッド.<br>
     * 指定されたクラス名のクラスインスタンスを作成して返却します。<br>
     * インスタンスの再生に失敗した場合はnullを返却する
     */
    DataAnalyzer createAnalyzer(String className) throws Exception {
        // DataAnalyzerクラス生成
        @SuppressWarnings("rawtypes")
        Class clazz = Class.forName(className);
        DataAnalyzer analyzer = (DataAnalyzer) clazz.newInstance();
        return analyzer;
    }
}
