//
//  ParserMain.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs;

import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;
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
     * 設定ファイルパスの格納用変数
     */
    static String fileName = null;

    /**
     * ログ用
     */
    private final Logger log = Logger.getLogger(ParserMain.class
            .getSimpleName());

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
        // メインクラス生成
        ParserMain main = new ParserMain();
        // クラス起動
        main.start();
    }

    /**
     * コンストラクタ
     */
    public ParserMain() {
    }

    /**
     * daemon初期化
     */
    @Override
    public void init(DaemonContext dc) throws DaemonInitException, Exception {
        log.info("パーサ機能を初期化します...");
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
        log.info("パーサ機能を開始します");
        
        // 構成ファイル読み込み
        ParserConfig config = ParserConfig.getInstance();
        try {
            config.loadYml(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("設定ファイルの読み込みに失敗しました。処理を中断します。");
            return;
        }

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
