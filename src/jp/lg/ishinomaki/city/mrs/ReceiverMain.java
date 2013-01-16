//
//  RecieverMain.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs;

import java.io.FileReader;
import java.util.Properties;
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.receiver.ReceiverThreadManager;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

/**
 * JMA,J-Alertからのメッセージ受信アプリケーションのメインクラスです。<br>
 * main()関数からのアプリ起動とjsvcからのデーモン起動のどちらにも対応しています。<br>
 * 
 */
public class ReceiverMain implements Daemon {

    /**
     * ログ用
     */
    private final Logger log = Logger.getLogger(ReceiverMain.class
            .getSimpleName());

    /**
     * データ受信スレッド管理インスタンス
     */
    private ReceiverThreadManager threadManager = null;

    /**
     * デフォルトコンストラクタ 特に処理なし daemon から呼ばれる?
     */
    public ReceiverMain() {
    }

    /**
     * スレッド管理クラスを取得します
     * 
     * @return
     */
    public ReceiverThreadManager getThreadManager() {
        return threadManager;
    }

    /**
     * スレッド管理クラスを設定します
     * 
     * @param threadManager
     */
    public void setThreadManager(ReceiverThreadManager threadManager) {
        this.threadManager = threadManager;
    }

    /**
     * main関数
     * 
     * @param args
     *            第一引数にスレッド定義プロパティファイル名を指定
     */
    public static void main(String[] args) {

        // 引数は1つでconfigファイルが指定されているはず
        if (args == null || args.length != 1) {
            System.err.println("パラメータが不正です。");
            System.err.println("パラメータにはプロパティファイルのパスをフルパスで指定してください。");
            return;
        }

        // configファイルの内容をAppConfigに保存
        Properties config = new Properties();
        try {
            config.load(new FileReader(args[0]));
        } catch (Exception e) {
            return;
        }
        // 必要な情報を取得
        String threads_file = config.getProperty("threads_file");
        String bch_file = config.getProperty("bch_file");
        
        AppConfig appConfig = AppConfig.getInstance();
        appConfig.putConfig("threads_file", threads_file);
        appConfig.putConfig("bch_file", bch_file);

        // メインクラス生成
        ReceiverMain main = new ReceiverMain();

        // スレッド管理インスタンス生成
        main.setThreadManager(new ReceiverThreadManager(threads_file));
        main.start();
    }

    /**
     * daemon初期化
     */
    @Override
    public void init(DaemonContext dc) throws DaemonInitException, Exception {
        log.info("データ受信機能を初期化します...");
        // 引数チェック
        String[] args = dc.getArguments();
        if (args == null || args.length != 1) {
            log.severe("パラメータが不正です。");
            log.severe("パラメータにはプロパティファイルのパスをフルパスで指定してください。");
            return;
        }

        // configファイルの内容をAppConfigに保存
        Properties config = new Properties();
        try {
            config.load(new FileReader(args[0]));
        } catch (Exception e) {
            return;
        }
        // 必要な情報を取得
        String threads_file = config.getProperty("threads_file");
        String bch_file = config.getProperty("bch_file");
        AppConfig appConfig = AppConfig.getInstance();
        appConfig.putConfig("threads_file", threads_file);
        appConfig.putConfig("bch_file", bch_file);

        // スレッド管理インスタンス生成
        setThreadManager(new ReceiverThreadManager(threads_file));
    }

    /**
     * daemon開始
     */
    @Override
    public void start() {
        log.info("データ受信機能を開始します");
        this.threadManager.start();
    }

    /**
     * daemon停止
     */
    @Override
    public void stop() {
        log.info("データ受信機能を停止します");
        this.threadManager.stop();
    }

    /**
     * daemon破棄
     */
    @Override
    public void destroy() {
        log.info("データ受信機能を破棄します");
    }

}