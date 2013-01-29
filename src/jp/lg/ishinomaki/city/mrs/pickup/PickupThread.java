//
//  PickupThread.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.pickup;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.Consts;
import jp.lg.ishinomaki.city.mrs.queue.QueuePopClient;

import org.newsclub.net.unix.AFUNIXSocketException;

/**
 * リモートキューを監視し、キューイングされたメッセージを1つずつ取り出し処理クラス(ハンドラー)へ処理を委託します。
 * 
 */
public class PickupThread extends Thread {

    /**
     * 当クラスのロガーインスタンス
     */
    private final Logger log = Logger.getLogger(PickupThread.class
            .getSimpleName());

    /**
     * リトライ回数
     */
    private int retryCount = 0;

    /**
     * キュー監視中に割り込み例外が発生した場合の最大リトライ回数<br>
     * 連続して失敗した回数がこの数値を超えた場合はスレッド処理を終了します。
     */
    private static final int RETRY_MAXCOUNT = 10;

    /**
     * コンストラクタです.<br>
     * RMIサーバのキューインスタンスを取得します。<br>
     * インスタンスの取得に失敗した場合は例外を投げます。<br>
     * このコンストラクタを呼ぶ前に必ずRMIサーバプロセスを起動しておいてください。
     */
    public PickupThread() throws IOException, NotBoundException {
    }

    /**
     * スレッド起動 リモートキューを無限ループで監視します。<br>
     */
    public void run() {

        // キューからポップするためのクライアント生成
        QueuePopClient popClient = new QueuePopClient();

        // ソケット接続確立
        while (true) {

            // 受信データ格納変数
            byte[] data = null;

            try {
                // キューからデータをポップ
                data = popClient.pop();

                // データがnullまたはデータ長が0の場合は処理しない
                if (data == null || data.length == 0) {
                    continue;
                }

                // メッセージ取得に成功->リトライカウントクリア
                retryCount = 0;

                // AFUNIXSocketException はサーバに接続できなかった場合に発生
                // サーバに接続できなかった場合は一定間隔でリトライする
            } catch (AFUNIXSocketException e) {
                // キュー機能が稼働していない場合は例外が発生する
                // その場合は一定時間待って再試行
                log.warning("キューに接続できませんでした。30秒後に再試行します...");

                try {
                    // 30秒スリープ
                    Thread.sleep(30 * 1000);
                } catch (Exception ie) {
                    // 特に処理なし
                }
                // 接続以外の例外発生した場合は規定回数リトライする
                continue;
            } catch (Exception e) {

                e.printStackTrace();

                // 失敗時のリトライカウント加算
                retryCount++;

                // リトライ回数を下回っている場合はリトライ実施
                if (retryCount < RETRY_MAXCOUNT) {
                    log.severe("キュー取り出し待ちスレッドで割り込みが発生しました！");
                    log.severe("再試行します。");

                    continue;
                }
                // リトライ回数以上の場合は処理終了
                else {
                    log.severe("キュー取り出し待ちスレッドで割り込みが発生しました！");
                    log.severe("最大リトライ回数を超えたため処理を中断します。");

                    break;
                }
            }

            // ----------------------------------------------------
            // キューからデータを取得した後はパース処理
            // ----------------------------------------------------
            log.info("キューからデータを取得しました");

            // 先頭3バイトがデータ種別を表すヘッダ部分のためヘッダ部とコンテンツ部に分割する
            byte[] dataType = new byte[3];
            byte[] contents = new byte[data.length - 3];
            System.arraycopy(data, 0, dataType, 0, dataType.length);
            System.arraycopy(data, 3, contents, 0, contents.length);

            String strDataType = new String(dataType); // dataTypeをString型に変換
            log.finest("キューから取得したデータの種類 -> [" + strDataType + "]");
            
            // データタイプにより処理クラスを変更する
            PickupDataHandler handler = null;
            // データ種別がXMLの場合
            if (strDataType.equals(Consts.QUEUE_DATA_TYPE_XML)) {
                // XML解析クラス
                handler = new JmaDataHandler();
            } else if (strDataType.equals(Consts.QUEUE_DATA_TYPE_TXT)) {
                // テキスト解析クラス
                handler = new TextDataHandler();
            } else if (strDataType.equals(Consts.QUEUE_DATA_TYPE_PDF)) {
                // PDF用テキスト解析クラス
            } else {
                log.warning("取得したデータが想定外のデータタイプです。後続処理を行いません。");
                continue;
            }

            // ハンドラーに処理依頼
            handler.handle(contents);
        }
    }

}
