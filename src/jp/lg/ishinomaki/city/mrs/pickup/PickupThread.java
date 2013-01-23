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

import jp.lg.ishinomaki.city.mrs.queue.QueuePopClient;

import org.dom4j.DocumentHelper;
import org.newsclub.net.unix.AFUNIXSocketException;

/**
 * リモートキューを監視し、キューイングされたメッセージを1つずつ取り出し処理クラス(ハンドラー)へ処理を委託します。
 * 
 */
public class PickupThread extends Thread {

    /**
     * 電文のデータタイプ
     */
    private enum DATA_TYPE {
        XML, TEXT, UNKNOWN
    };

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

            // データタイプにより処理クラスを変更する
            PickupDataHandler handler = null;
            DATA_TYPE dataType = getDataType(data);
            if (dataType == DATA_TYPE.XML) {
                // XML解析クラス
                handler = new JmaDataHandler();
            } else if (dataType == DATA_TYPE.TEXT) {
                // テキスト解析クラス
                handler = new TextDataHandler();
            } else if (dataType == DATA_TYPE.UNKNOWN) {
                log.warning("取得したデータが想定外のデータタイプです。後続処理を行いません。");
                continue;
            }

            // ハンドラーに処理依頼
            handler.handle(data);
        }
    }

    /**
     * データのタイプを取得します.<br>
     * タイプの判定方法は、引数データがXMLドキュメントオブジェクトに変換できる場合はXML、変換できない場合はテキストであるとします。
     * 
     * @param data
     * @return DATA_TYPE UNKNOWN or TEXT or XML
     */
    private DATA_TYPE getDataType(byte[] data) {

        // String に変換
        String str = null;
        try {
            str = new String(data, "utf-8");
        } catch (Exception e) {
            log.finest("データをStringに変換するのに失敗。データタイプ[UNKNOWN]を返却。");
            return DATA_TYPE.UNKNOWN;
        }

        // xmlドキュメントに変換可能か確認
        // 変換不可であればテキスト形式と判断する
        try {
            DocumentHelper.parseText(str);
        } catch (Exception e) {
            log.finest("データをXMLに変換するのに失敗。データタイプ[TEXT]を返却。");
            return DATA_TYPE.TEXT;
        }

        return DATA_TYPE.XML;
    }

}
