//
//  ReceiverDataAnalyzeTask.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.receiver;

import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.analyzer.DataAnalyzer;
import jp.lg.ishinomaki.city.mrs.queue.QueueClient;
import jp.lg.ishinomaki.city.mrs.utils.FileUtils;
import jp.lg.ishinomaki.city.mrs.utils.StringUtils;

/**
 * 受信データを解析しキューへ登録するためのクラスです.<br>
 * 受信スレッドとは別スレッドで処理するためRunnableインターフェースを実装しています。<br>
 * 
 */
public class ReceiverDataAnalyzeTask implements Runnable {

    /**
     * ロガーインスタンス
     */
    private final Logger log = Logger.getLogger(ReceiverDataAnalyzeTask.class
            .getSimpleName());

    /**
     * 解析対象のデータ
     */
    byte[] data;

    /**
     * ファイル保存用パス 設定されていない場合はファイルを保存しない
     */
    String outputPath = null;

    /**
     * データ解析クラス
     */
    DataAnalyzer analyzer = null;

    /**
     * 入力元識別子
     */
    String inputId = null;

    /**
     * 動作モード
     */
    int mode = 0;

    /**
     * コンストラクタ
     * 
     * @param data
     *            解析対象データ
     */
    public ReceiverDataAnalyzeTask(byte[] data, String outputPath,
            DataAnalyzer analyzer, String inputId, int mode) {
        this.data = data;
        this.outputPath = outputPath;
        this.analyzer = analyzer;
        this.inputId = inputId;
        this.mode = mode;
    }

    /**
     * データ解析処理
     */
    @Override
    public void run() {

        // 処理対象のデータがない場合は処理終了
        if (data == null) {
            return;
        }

        // -------------------------------------------
        // ユーザデータ解析
        // -------------------------------------------
        try {
            analyzer.analyze(data);
        } catch (Exception e) {
            log.severe("データ解析でエラーが発生しました。");
            e.printStackTrace();
            log.severe("データ解析を中断します。キューにも登録されません。");
            return;
        }

        // -------------------------------------------
        // ユーザデータから本文取得
        // -------------------------------------------
        byte[] contents = analyzer.getContents();

        // 処理対象のデータがない場合は処理終了
        if (contents == null || contents.length == 0) {
            return;
        }

        // -------------------------------------------
        // 本文内容をアーカイブとして保存
        // -------------------------------------------
        createOutputFile(contents);

        // -------------------------------------------
        // 本文データをキューに登録
        // -------------------------------------------
        // キュー管理インスタンス取得
        QueueClient queueClient = new QueueClient();
        try {
            // キューにデータをセット
            byte[] message = createMessage(contents);
            queueClient.push(message);
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("キュー機能へのデータ登録に失敗しました。キュー機能が正常に稼働しているか確認してください。");
        }
    }

    /**
     * アーカイブファイルを作成するための内部メソッド.<br>
     * 
     * @param contents 本文データ
     */
    void createOutputFile(byte[] contents) {
        // -------------------------------------------
        // 本文データをファイルに保存
        // TODO ファイル名ルールを決定する必要あり
        // -------------------------------------------
        if (StringUtils.isBlank(outputPath) == false) {
            // ファイル名生成
            // ファイル名はとりあえず"日時刻としておく
            String fileName = FileUtils.genFileName("");

            // ファイル生成
            boolean isSuccessSaveFile = FileUtils.saveContentsAsFile(contents,
                    outputPath, fileName);
            log.finest("ファイル生成結果 -> [" + isSuccessSaveFile + "]");
            if (isSuccessSaveFile == false) {
                log.warning("受信データのファイル保存が失敗しました。ファイル名 [" + fileName + "]");
                log.warning("処理は続行します。");
            }
        }
    }

    /**
     * キュー登録用のメッセージ(byte配列)を作成します.<br>
     * 引数の本文に対して必要なヘッダを付与します。
     * 
     * @param contents 本文データ
     */
    byte[] createMessage(byte[] contents) {

        // ---------------------------------------------------------
        // キューにプットするデータにヘッダを付与
        // ヘッダの仕様は以下のとおり
        // length:1 動作モード(0:通常,1:訓練,2:試験)
        // lenght:3 入力元識別子(JMA,JALなど)
        // length:3 データ種別(XML,PDF,TXT,BUFなど)
        // ---------------------------------------------------------
        // モード設定
        byte[] hm = null; // あとで使いやすいよう変数をわざと短めに(header_modeの意味)
        String strMode = String.valueOf(mode);
        hm = strMode.getBytes();
        // 各データのレングスを変数に保持(これもあとで使いやすいように
        int hml = hm.length;
        // 入力元識別子設定
        byte[] hi = inputId.getBytes();
        int hil = hi.length;
        // データ種別
        byte[] ht = analyzer.getDataType().getBytes();
        int htl = ht.length;

        // ヘッダー + データをキュー設定用バイト配列に設定
        byte[] message = new byte[contents.length + hml + hil + htl];
        // 配列の先頭にヘッダを付与
        System.arraycopy(hm, 0, message, 0, hml);
        System.arraycopy(hi, 0, message, hml, hil);
        System.arraycopy(ht, 0, message, hml + hil, htl);
        // ヘッダ以降にcontentsのバイト配列を設定
        System.arraycopy(contents, 0, message, hml + hil + htl, contents.length);

        return message;
    }
}
