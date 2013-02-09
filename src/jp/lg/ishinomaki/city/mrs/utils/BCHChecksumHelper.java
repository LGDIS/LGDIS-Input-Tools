//
//  BCHChecksumHelper.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.utils;

/**
 * BCHのQCチェックサムによるチェックを行うためのクラス
 * 
 */
public class BCHChecksumHelper {

    /**
     * QCチェックサム実行
     * 
     * @param bch
     *            BCH全データ
     * @return true:チェックOK false:チェックNG
     */
    public static boolean check(String bch, String checksum) {

        // ①足し算用32ビット変数準備
        int sum = 0;

        // ②先頭から2バイト(16ビット)ずつ取り出し、①の変数に加算する
        // ただしQCチェックサム部分(インデックス5)はオールゼロとみなすため足し算をスキップ
        for (int i = 0; i < 10; i++) {
            if (i == 5) {
                // QCチェックサム部分のオクテットのため足し算しない
                continue;
            }
            // 16ビット分を取り出し、intに変換して足し算
            int index = i * 16;
            String str = bch.substring(index, index + 16);
            sum = sum + Integer.parseInt(str, 2);
        }

        // ③足し算した32ビットの上位16ビットが0でない場合は、上位16ビットを下位16ビットに加算する
        // 上位16ビットがゼロになるまで繰り返す
        do {
            // sum を32ビット文字列表現に
            String strSum = BCHChecksumHelper.fullZero(
                    Integer.toBinaryString(sum), 32);

            // 上位16ビットを取得
            String strHighOrder = strSum.substring(0, 16);
            int iHighOrder = Integer.parseInt(strHighOrder, 2);

            // 上位16ビットがゼロの場合はループ終了
            if (iHighOrder == 0) {
                break;
            }

            // 上位16ビットがゼロでない場合は下位16ビットに加算する
            String strLowOrder = strSum.substring(16, 32);
            int iLowOrder = Integer.parseInt(strLowOrder, 2);
            sum = iHighOrder + iLowOrder;

        } while (true);

        // ④下位16ビット(sum)をビット反転
        sum = ~sum;

        // ⑤下位16ビット(sum)を16ビット文字列表現に変換
        String strSum = Integer.toBinaryString(sum);
        strSum = strSum.substring(16);

        // ⑥引数checksumの値と⑤で導出した文字列(チェックサム)が同じ場合はチェックサムOK
        if (checksum.equals(strSum)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 0埋め処理用ユーティリティメソッド
     * 
     * @param tgt
     * @param figure
     * @return
     */
    private static String fullZero(String tgt, int figure) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < figure - tgt.length(); i++) {
            sb.append("0");
        }
        sb.append(tgt);
        return sb.toString();
    }

}
