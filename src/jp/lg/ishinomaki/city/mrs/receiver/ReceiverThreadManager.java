//
//  ReceiverThreadManager.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.receiver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.analyzer.DataAnalyzer;
import jp.lg.ishinomaki.city.mrs.receiver.jma.JmaServerSocketControl;
import jp.lg.ishinomaki.city.mrs.utils.ServerProperties;

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
    public ReceiverThreadManager(String properyFilePath) {

        ServerProperties properties = null;
        // プロパティファイル読み込み
        try {
            properties = new ServerProperties(properyFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            // プロパティファイル読み込みに失敗した場合は起動中止
            log.severe(e.getMessage());
            log.severe("プロパティファイルの読み込みに失敗したためアプリケーション起動を中断します。");
            return;
        }

        // プロパティファイルのルールに従って各スレッド起動
        try {
            // スレッド起動
            threadManager = new HashMap<String, ReceiverThread>(); // スレッド管理テーブル生成

            List<String> threadNames = properties.getThreadNames();
            for (String threadName : threadNames) {
                // スレッド名からプロパティの各種設定を取得
                String ipAddress = properties
                        .getIpAddressFromThreadName(threadName);

                // ポート番号
                int portNo = properties.getPortNoFromThreadName(threadName);

                // DataAnalyzerクラス名
                String analyzerClassName = properties
                        .getAnalyzerFromThreadName(threadName);

                // ファイル出力先
                String outputPath = properties
                        .getOutputPathFromThreadName(threadName);

                // ソケット制御生成
                JmaServerSocketControl socketControl = new JmaServerSocketControl(
                        ipAddress, portNo);

                // DataAnalyzerクラス生成
                DataAnalyzer analyzer = null;
                try {
                    @SuppressWarnings("rawtypes")
                    Class clazz = Class.forName(analyzerClassName);
                    if (clazz != null) {
                        analyzer = (DataAnalyzer) clazz.newInstance();
                    }
                } catch (ClassNotFoundException e) { // by Class.forName
                    e.printStackTrace();
                    log.severe("電文解析クラスの生成に失敗したためアプリケーション起動を中断します。");
                    return;
                } catch (InstantiationException e) { // by newInstance()
                    e.printStackTrace();
                    log.severe("電文解析クラスの生成に失敗したためアプリケーション起動を中断します。");
                    return;
                } catch (IllegalAccessException e) { // by newInstance()
                    e.printStackTrace();
                    log.severe("電文解析クラスの生成に失敗したためアプリケーション起動を中断します。");
                    return;
                }

                // スレッド生成
                ReceiverThread thread = new ReceiverThread(threadName,
                        socketControl, outputPath, analyzer);

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
}
