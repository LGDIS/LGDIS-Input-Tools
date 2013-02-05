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
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.analyzer.DataAnalyzer;
import jp.lg.ishinomaki.city.mrs.receiver.jma.JmaServerSocketControl;

/**
 * スレッド管理クラス
 * 
 */
public class ReceiverThreadManager {

    /**
     * ロガーインスタンス
     */
    private final Logger log = Logger.getLogger(ReceiverThreadManager.class
            .getSimpleName());

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
    public ReceiverThreadManager() {

        // プロパティファイルのルールに従って各スレッド起動
        try {
            // スレッド起動
            threadManager = new HashMap<String, ReceiverThread>(); // スレッド管理テーブル生成

            // スレッド定義情報を取得
            Map<String, Object> threads = ReceiverConfig.getInstance()
                    .getThreads();
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
                String outputPath = (String) threadInfo
                        .get(ReceiverConfig.OUTPUT);

                // ソケット制御生成
                JmaServerSocketControl socketControl = new JmaServerSocketControl(
                        ipAddress, portNo);

                // 入力元識別子
                String inputId = (String) threadInfo
                        .get(ReceiverConfig.INPUT_ID);

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

        } catch (Exception e) {
            e.printStackTrace();
            log.severe("スレッド起動中に例外が発生したため起動を中止。");
            return;
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
    private DataAnalyzer createAnalyzer(String className) {
        // DataAnalyzerクラス生成
        DataAnalyzer analyzer = null;
        try {
            @SuppressWarnings("rawtypes")
            Class clazz = Class.forName(className);
            if (clazz != null) {
                analyzer = (DataAnalyzer) clazz.newInstance();
            }
        } catch (ClassNotFoundException e) { // by Class.forName
            e.printStackTrace();
            log.severe("電文解析クラスの生成に失敗したためアプリケーション起動を中断します。");
        } catch (InstantiationException e) { // by newInstance()
            e.printStackTrace();
            log.severe("電文解析クラスの生成に失敗したためアプリケーション起動を中断します。");
        } catch (IllegalAccessException e) { // by newInstance()
            e.printStackTrace();
            log.severe("電文解析クラスの生成に失敗したためアプリケーション起動を中断します。");
        }
        return analyzer;
    }
}
