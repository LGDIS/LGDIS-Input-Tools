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

    /**
     * アプリケーションのメイン関数
     * 
     * @param args
     */
    public static void main(String[] args) {
        new QueueMain();
    }

    /**
     * コンストラクタ
     */
    public QueueMain() {
        try {
            // デーモン初期化処理+開始
            // main->コンストラクタから実行する場合はinitの引数にnullを渡すことで
            // デーモンAPIから開始された場合と区別している
            this.init(null);
            this.start();
        } catch (DaemonInitException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        try {
            // キューサーバを起動
            new QueuePushServer().start();
            new QueuePopServer().start();
            
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("RMIサーバで割り込みが発生した可能性があります。キュー管理機能を再起動してください。");
        }

    }

    /**
     * daemon停止
     */
    @Override
    public void stop() {
        log.info("キュー管理機能を停止します");
    }

    /**
     * daemon破棄
     */
    @Override
    public void destroy() {
        log.info("キュー管理機能を破棄します");
    }

}
