//
//  ParserMain.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs;

import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.pickup.PickupThread;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

/**
 * 「外部入力機能」のパーサ機能用プロセスを起動するメインクラスです。<br>
 * main()関数からのアプリ起動とjsvcからのデーモン起動のどちらにも対応しています。<br>
 */
public class ParserMain implements Daemon {

    /**
     * ログ用
     */
    private final Logger log = Logger
            .getLogger(ParserMain.class.getSimpleName());

    /**
     * アプリケーションのメイン関数
     * 
     * @param args
     */
    public static void main(String[] args) {
        new ParserMain();
    }

    /**
     * コンストラクタ
     */
    public ParserMain() {
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
        log.info("パーサ機能を初期化します...");
    }

    /**
     * daemon開始
     */
    @Override
    public void start() {
        log.info("パーサ機能を開始します");

        // PickupThread起動
        try {
            PickupThread thread = new PickupThread();
            thread.start();
            
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("PickupThreadの起動に失敗しました！パーサ機能を再起動してください。");
        }

    }

    /**
     * daemon停止
     */
    @Override
    public void stop() {
        log.info("パーサ機能を停止します");
    }

    /**
     * daemon破棄
     */
    @Override
    public void destroy() {
        log.info("パーサ機能を破棄します");
    }

}
