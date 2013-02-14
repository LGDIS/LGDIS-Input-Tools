//
//  QueueMain.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs;

import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.queue.QueueConfig;
import jp.lg.ishinomaki.city.mrs.queue.QueuePopServer;
import jp.lg.ishinomaki.city.mrs.queue.QueuePushServer;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

/**
 * 「外部入力機能」のキュー機能用プロセスを起動するメインクラスです。<br>
 * main()関数からのアプリ起動とjsvcからのデーモン起動のどちらにも対応しています。<br>
 * 
 */
public class QueueMain implements Daemon {

    /**
     * ログ用
     */
    private final Logger log = Logger
            .getLogger(QueueMain.class.getSimpleName());

    /**
     * 設定ファイルパスの格納用変数
     */
    static String fileName = null;

    private QueuePushServer pushServer = null;
    private QueuePopServer popServer = null;

    /**
     * アプリケーションのメイン関数
     * 
     * @param args
     */
    public static void main(String[] args) {

        // 引数は1つでconfigファイルが指定されているはず
        if (args == null || args.length != 1) {
            System.err.println("パラメータが不正です。");
            System.err.println("パラメータには設定ファイルのパスをフルパスで指定してください。");
            return;
        }

        fileName = args[0];

        QueueMain main = new QueueMain();
        main.start();
    }

    /**
     * コンストラクタ
     */
    public QueueMain() {
    }

    /**
     * daemon初期化
     */
    @Override
    public void init(DaemonContext dc) throws DaemonInitException, Exception {
        log.info("キュー管理機能を初期化します...");
        // 引数チェック
        String[] args = dc.getArguments();
        if (args == null || args.length != 1) {
            log.severe("パラメータが不正です。");
            log.severe("パラメータには設定ファイルのパスをフルパスで指定してください。");
            return;
        }
        fileName = args[0];

    }

    /**
     * daemon開始
     */
    @Override
    public void start() {
        log.info("キュー管理機能を開始します");

        // 構成ファイル読み込み
        QueueConfig config = QueueConfig.getInstance();
        try {
            config.loadYml(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("設定ファイルの読み込みに失敗しました。処理を中断します。");
            return;
        }

        if (pushServer == null) {
            pushServer = new QueuePushServer();
        }
        if (popServer == null) {
            popServer = new QueuePopServer();
        }
        try {
            // キューサーバを起動
            pushServer.start();
            popServer.start();

        } catch (Exception e) {
            e.printStackTrace();
            log.severe("キュースレッドに対して割り込みが発生した可能性があります。キュー管理機能を再起動してください。");
        }

    }

    /**
     * daemon停止
     */
    @Override
    public void stop() {
        log.info("キュー管理機能を停止します");
        pushServer.interrupt();
        popServer.interrupt();
        pushServer = null;
        popServer = null;
    }

    /**
     * daemon破棄
     */
    @Override
    public void destroy() {
        log.info("キュー管理機能を破棄します");
        if (pushServer != null) {
            pushServer.interrupt();
            pushServer = null;
        }
        if (popServer != null) {
            popServer.interrupt();
            popServer = null;
        }
    }

}
