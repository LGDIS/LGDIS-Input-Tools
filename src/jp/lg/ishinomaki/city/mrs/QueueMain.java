//
//  QueueMain.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs;

import java.util.logging.Logger;

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

    private QueuePushServer pushServer = null;
    private QueuePopServer popServer = null;
    
    /**
     * アプリケーションのメイン関数
     * 
     * @param args
     */
    public static void main(String[] args) {
        QueueMain main = new QueueMain();
        try {
            main.init(null);
            main.start();
        } catch (DaemonInitException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    }

    /**
     * daemon開始
     */
    @Override
    public void start() {
        log.info("キュー管理機能を開始します");
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
