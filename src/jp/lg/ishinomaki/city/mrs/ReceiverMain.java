//
//  RecieverMain.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs;

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
    private final Logger log = Logger.getLogger(ReceiverMain.class.getSimpleName());
    
    /**
     * データ受信スレッド管理インスタンス
     */
    private ReceiverThreadManager threadManager = null;

    /**
     * データ受信スレッド内容定義プロパティ
     */
    private String threadProperties = null;

    /**
     * デフォルトコンストラクタ 特に処理なし daemon から呼ばれる?
     */
    public ReceiverMain() {
    }

    /**
     * コンストラクタ main関数から呼ばれる想定
     * 
     * @param args
     *            main関数の引数
     */
    public ReceiverMain(String[] args) {

        // 引数チェック
        if (args == null || args.length != 1) {
            log.severe("パラメータが不正です。");
            log.severe("パラメータにはプロパティファイルのパスをフルパスで指定してください。");
            return;
        }

        // 受信データスレッド定義プロパティファイル保存
        this.threadProperties = args[0];

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
     * main関数
     * 
     * @param args
     *            第一引数にスレッド定義プロパティファイル名を指定
     */
    public static void main(String[] args) {
        new ReceiverMain(args);
    }

    /**
     * daemon初期化
     */
    @Override
    public void init(DaemonContext dc) throws DaemonInitException, Exception {
        log.info("データ受信機能を初期化します...");
        // DamonContextがnullの場合はmain関数からのコール
        if (dc == null) {
            // すでにthreadPropertiesは保存してあるはずなので処理しない
        } else {
            // コマンドライン引数取得
            String[] args = dc.getArguments();
            // 引数チェック
            if (args == null || args.length != 1) {
                log.severe("パラメータが不正です。");
                log.severe("パラメータにはプロパティファイルのパスをフルパスで指定してください。");
                return;
            }

            // スレッド定義プロパティファイル保存
            this.threadProperties = args[0];
        }
        
        // スレッド管理インスタンス生成
        this.threadManager = new ReceiverThreadManager(this.threadProperties);
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