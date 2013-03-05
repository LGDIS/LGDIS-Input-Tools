//
//  PickupThread.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.pickup;

import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.Consts;
import jp.lg.ishinomaki.city.mrs.queue.QueueClient;

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
     */
    public PickupThread() {
    }

    /**
     * スレッド起動 リモートキューを無限ループで監視します.<br>
     * 
     */
    public void run() {

        // キューからポップするためのクライアント生成
        QueueClient popClient = new QueueClient();

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

            // データタイプ
            String dataType = getDataType(data);
            // モード
            int mode = getMode(data);
            // 入力元識別子
            String inputId = getInputId(data);

            // データタイプにより処理クラスを変更する
            PickupDataHandler handler = null;
            // データ種別がXMLの場合
            if (dataType.equals(Consts.DATA_TYPE_XML)) {
                // データ入力元がJMAまたはJ-Alertの場合
                if (Consts.INPUT_ID_JAL.equals(inputId)
                        || Consts.INPUT_ID_JMA.equals(inputId)) {
                    // JMA用のXMLハンドルクラス
                    handler = new JmaXmlDataHandler(mode, inputId);
                } else if (Consts.INPUT_ID_KSN.equals(inputId)) {
                    // 河川用のXMLハンドルクラス
                    handler = new KsnXmlDataHandler(mode, inputId);
                }
            } else if (dataType.equals(Consts.DATA_TYPE_TXT)) {
                // テキスト用ハンドルクラス
                handler = new TextDataHandler(mode, inputId);
            } else if (dataType.equals(Consts.DATA_TYPE_PDF)) {
                // PDF用ハンドルクラス
                handler = new PdfDataHandler(mode, inputId);
            } else if (dataType.equals(Consts.DATA_TYPE_TAR)) {
                // TAR用ハンドルクラス
                handler = new TarDataHandler(mode, inputId);
            } else {
                log.warning("キューから取得したデータのデータ種類に対して処理クラスが設定されていません。");
                continue;
            }

            byte[] contents = getContents(data);
            // ハンドラーに処理依頼
            handler.handle(contents);

        }
    }

    /**
     * キューから取得したデータの稼働モードを取得する内部メソッド
     * 
     * @param data
     *            キューから取得したデータ全体
     * @return int 稼働モード 0:通常 1:訓練 2:試験
     */
    int getMode(byte[] data) {
        byte[] mode = new byte[1];
        System.arraycopy(data, 0, mode, 0, 1);
        String strMode = new String(mode);
        log.finest("キューから取得したデータのモード -> [" + strMode + "]");
        // モードを数値型に変換
        int iMode = Integer.parseInt(strMode);
        return iMode;
    }

    /**
     * キューから取得したデータの入力元識別子を取得する内部メソッド
     * 
     * @param data
     *            キューから取得したデータ全体
     * @return String 入力元識別子
     */
    String getInputId(byte[] data) {
        byte[] inputId = new byte[3];
        System.arraycopy(data, 1, inputId, 0, 3);
        String strInputId = new String(inputId);
        log.finest("キューから取得した入力識別ID -> [" + strInputId + "]");
        return strInputId;
    }

    /**
     * キューから取得したデータの種別を取得する内部メソッド
     * 
     * @param data
     *            キューから取得したデータ全体
     * @return String データ種別
     */
    String getDataType(byte[] data) {
        byte[] dataType = new byte[3];
        System.arraycopy(data, 4, dataType, 0, 3);
        String strDataType = new String(dataType);
        log.finest("キューから取得したデータ種別 -> [" + strDataType + "]");
        return strDataType;
    }

    /**
     * キューから取得したデータの本文部分を取得する内部メソッド
     * 
     * @param data
     *            キューから取得したデータ全体
     * @return byte[] 本文部分のデータのみ取得する
     */
    byte[] getContents(byte[] data) {
        byte[] contents = new byte[data.length - 7];
        System.arraycopy(data, 7, contents, 0, contents.length);
        return contents;
    }

}
