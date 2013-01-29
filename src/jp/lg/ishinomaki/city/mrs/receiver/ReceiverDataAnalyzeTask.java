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
import jp.lg.ishinomaki.city.mrs.queue.QueuePushClient;
import jp.lg.ishinomaki.city.mrs.utils.FileUtilities;

/**
 * 受信データを解析しキューへ登録するためのクラスです.<br>
 * 受信スレッドとは別スレッドで処理するためRunnableインターフェースを実装しています。<bR>
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
    private byte[] data;

    /**
     * ファイル保存用パス 設定されていない場合はファイルを保存しない
     */
    private String outputPath = null;

    /**
     * データ解析クラス
     */
    private DataAnalyzer analyzer = null;

    /**
     * コンストラクタ
     * 
     * @param data
     *            解析対象データ
     */
    public ReceiverDataAnalyzeTask(byte[] data) {
        this.data = data;
    }

    /**
     * データ解析処理
     */
    @Override
    public void run() {

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

        // -------------------------------------------
        // 本文データをファイルに保存
        // TODO ファイル名ルールを決定する必要あり
        // -------------------------------------------
        if (outputPath != null) {
            // ファイル名生成
            // ファイル名はとりあえず"test"としておく
            String fileName = FileUtilities.genFileName("test");

            // ファイル生成
            boolean isSuccessSaveFile = FileUtilities.saveContentsAsFile(
                    contents, outputPath, fileName);
            log.finest("ファイル生成結果 -> [" + isSuccessSaveFile + "]");
            if (isSuccessSaveFile == false) {
                log.warning("受信データのファイル保存が失敗しました。ファイル名 [" + fileName + "]");
                log.warning("処理は続行します。");
            }
        }

        // -------------------------------------------
        // 本文データをキューに登録
        // -------------------------------------------
        // キュー管理インスタンス取得
        QueuePushClient queuePushClient = new QueuePushClient();
        try {
            // cotentsのバイト配列先頭にデータ種別を表す識別子(3byte)を付与する
            byte[] type = analyzer.getDataType().getBytes();
            byte[] data = new byte[contents.length + type.length];
            // 配列の先頭にヘッダを付与
            System.arraycopy(type, 0, data, 0, type.length);
            // ヘッダ以降にcontentsのバイト配列を設定
            System.arraycopy(contents, 0, data, type.length, contents.length);
            // キューにデータをセット
            queuePushClient.push(data);
        } catch (Exception e) {
            e.printStackTrace();
            log.severe("キュー機能へのデータ登録に失敗しました。キュー機能が正常に稼働しているか確認してください。");
        }
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public DataAnalyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(DataAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

}
