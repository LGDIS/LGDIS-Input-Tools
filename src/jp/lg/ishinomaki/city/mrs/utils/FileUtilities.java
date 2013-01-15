//
//  FileUtilities.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ファイル関連のユーティリティメソッドを集めたクラスです。
 * 
 */
public class FileUtilities {

    /**
     * 第一引数で指定したデータをを第二,第三引数で指定したパスのファイルに出力します。 出力する際に使用する文字コードは"utf-8"を使用します。
     * 
     * @param contents
     *            ファイル内容
     * @param outputPath
     *            ファイルパス
     * @return boolean true:成功 false:失敗
     */
    public static boolean saveContentsAsFile(byte[] contents,
            String outputPath, String fileName) {

        // 引数チェック
        if (contents == null || outputPath == null || fileName == null) {
            return false;
        }

        // ディレクトリの存在確認
        File file = new File(outputPath);
        if (!file.exists()) {
            // ディレクトリがない場合は作成しておく
            try {
                boolean ret = file.mkdirs();
                if (ret == false) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        FileOutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // 出力ストリームを使用してファイル出力
            outputStream = new FileOutputStream(outputPath + "/" + fileName);
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(contents, 0, contents.length);
            bufferedOutputStream.flush();

        } catch (Exception e) {
            // ファイルIOのエラーはアプリで解決できない
            e.printStackTrace();
            return false;
        } finally {
            try {
                bufferedOutputStream.close();
                outputStream.close();
            } catch (Exception ex) {
                // ファイルIOのエラーはアプリで解決できない
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * ファイル名作成用ユーティリティメソッド
     */
    public static String genFileName(String type) {
        Date nowDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss-SSS");
        String dateString = sdf.format(nowDate);
        String retStr = type + dateString;
        return retStr;
    }

    /**
     * テスト用メソッド J-Alertから送信されたバイナリデータをファイルに保存します。 ファイル名は日付を自動的に付与します。
     * 
     * @param data
     *            バイナリデータ
     */
    public static void saveDataAsFile(byte[] data, String outputPath) {
        // 引数チェック
        if (data == null || outputPath == null) {
            return;
        }

        // ディレクトリの存在確認
        File file = new File(outputPath);
        if (!file.exists()) {
            // ディレクトリがない場合は作成しておく
            try {
                boolean ret = file.mkdirs();
                if (ret == false) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        // ファイル名を
        Date nowDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
        String fileName = sdf.format(nowDate);
        
        FileOutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // 出力ストリームを使用してファイル出力
            outputStream = new FileOutputStream(outputPath + "/" + fileName);
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(data, 0, data.length);
            bufferedOutputStream.flush();

        } catch (Exception e) {
            // ファイルIOのエラーはアプリで解決できない
            e.printStackTrace();
            return;
        } finally {
            try {
                bufferedOutputStream.close();
                outputStream.close();
            } catch (Exception ex) {
                // ファイルIOのエラーはアプリで解決できない
                ex.printStackTrace();
                return;
            }
        }
    }

}
