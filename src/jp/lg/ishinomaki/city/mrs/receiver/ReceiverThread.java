//
//  ReceiverThread.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.receiver;

import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.analyzer.DataAnalyzer;
import jp.lg.ishinomaki.city.mrs.receiver.jma.JmaServerSocketControl;
import jp.lg.ishinomaki.city.mrs.receiver.jma.JmaServerSocketControlDelegate;

/**
 * サーバ受信をマルチスレッド化するためのスレッドクラスです。<br>
 * JmaServerSocketControlを使用してソケット接続する部分を新規スレッドで実行します。
 * 
 */
public class ReceiverThread extends Thread implements
        JmaServerSocketControlDelegate {

    /**
     * 当クラスのロガーインスタンス
     */
    private final Logger log = Logger.getLogger(ReceiverThread.class
            .getSimpleName());

    /**
     * スレッド名
     */
    private String threadName = null;

    /**
     * 本文出力先ディレクトリ
     */
    private String outputPath = null;

    /**
     * サーバソケット制御インスタンス
     */
    private JmaServerSocketControl serverSocketControl = null;
    
    /**
     * データ解析クラスインスタンス.<br>
     * 1スレッドに対して1つのデータ解析クラスをのデータ受信機能の起動プロパティで指定
     */
    private DataAnalyzer analyzer = null;

    /**
     * コンストラクタです。
     * 
     * @param serverSocketControl
     */
    public ReceiverThread(String threadName,
            JmaServerSocketControl serverSocketControl, String outputPath,
            DataAnalyzer analyzer) {

        // スレッド名保存
        this.threadName = threadName;
        // ソケットコントローラインスタンス保存
        this.serverSocketControl = serverSocketControl;
        // スレッド名を設定
        this.serverSocketControl.setThreadName(threadName);
        // デリゲート設定
        this.serverSocketControl.setDelegate(this);

        // 本文ファイル出力先保存
        this.outputPath = outputPath;
        
        // データ解析クラス
        this.analyzer = analyzer;
    }

    /**
     * デリゲートメソッド クライアントとの接続時にコールされます。
     */
    public void acceptConnection(Socket socket) {
        // いまのところ特に処理なし
    }

    /**
     * デリゲートメソッド クライアントからデータを受信した際にコールされます。
     */
    public void receiveData(byte[] data) {
        log.finest("receiveData");

        // 解析用タスク実行(別スレッドで実施するため)
        ReceiverDataAnalyzeTask task = new ReceiverDataAnalyzeTask(data);
        // 解析結果出力パスと解析用クラスを設定
        task.setOutputPath(outputPath);
        task.setAnalyzer(analyzer);
        
        // Executorオブジェクトの生成
        Executor executor = Executors.newSingleThreadExecutor();
        // タスクの実行
        executor.execute(task);
    }

    /**
     * スレッド起動 SocketControlの接続待ちスレッドを起動します。
     */
    public void run() {
        log.info("[" + this.threadName + "] スレッド起動");

        // サーバソケットの接続準備 + 接続待ち開始
        boolean ret = this.serverSocketControl.setup();
        if (ret == false) {
            log.severe("[" + this.threadName + "] スレッド起動に失敗しました。スレッドを停止します。");
            return;
        }
    }

    /**
     * このスレッドのスレッド名を取得します。
     * 
     * @return String スレッド名
     */
    public String getThreadName() {
        return this.threadName;
    }

    /**
     * このスレッドを停止します。
     */
    public void done() {
        log.info("[" + this.threadName + "] スレッド停止");
        // サーバのソケット接続制御のクローズ処理
        this.serverSocketControl.closeServerSocket();
        // ソケット停止から1秒待ちスレッド中断
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        this.interrupt();
    }
}
