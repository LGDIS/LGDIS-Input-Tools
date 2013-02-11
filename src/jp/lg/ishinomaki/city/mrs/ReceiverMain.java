//
//  RecieverMain.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs;

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.receiver.ReceiverConfig;
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
     * 設定ファイルパス格納用変数
     */
    static String fileName;

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
     * コンストラクタ
     */
    public ReceiverMain() {
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

        // 設定ファイル名を保存
        fileName = args[0];

        // メインクラス生成
        ReceiverMain main = new ReceiverMain();
        // 処理開始
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

        fileName = args[0];

    }

    /**
     * daemon開始
     */
    @Override
    public void start() {
        log.info("データ受信機能を開始します");

        // 構成ファイル読み込み
        ReceiverConfig config = ReceiverConfig.getInstance();
        try {
            config.loadYml(fileName);
        } catch (FileNotFoundException e) {
            log.severe("プロパティファイルの読み込みに失敗しました。データ受信機能の開始を中断します。");
            e.printStackTrace();
            return;
        }

        // スレッド管理インスタンス生成
        try {
            threadManager = new ReceiverThreadManager();
        } catch (Exception e) {
            log.severe("スレッド管理インスタンスの生成に失敗しました。データ受信機能の開始を中断します。");
            e.printStackTrace();
            return;
        }

        // 受信スレッド開始
        threadManager.start();
    }

    /**
     * daemon停止
     */
    @Override
    public void stop() {
        log.info("データ受信機能を停止します");
        threadManager.stop();
        threadManager = null;
    }

    /**
     * daemon破棄
     */
    @Override
    public void destroy() {
        log.info("データ受信機能を破棄します");
        if (threadManager != null) {
            threadManager.stop();
            threadManager = null;
        }
    }

}