//
//  JmaMessage.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.receiver.jma;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Jmaから受信したデータのラッパークラスです。
 * 
 */
public class JmaMessage {

    /**
     * 受信データ(生データ)
     */
    private byte[] data = null;

    /**
     * ソケットヘッダから取得したメッセージ長
     */
    private int messageLength;

    /**
     * ソケットヘッダから取得したメッセージタイプ
     */
    private String messageType;

    /**
     * 制御レコード種別 (制御用レコードのみ)
     */
    private String controlType;

    /**
     * チェックポイント電文であるかを表す
     */
    private boolean isCheckPoint = false;

    /**
     * ヘルスチェック電文であるかを表す
     */
    private boolean isHealthCheck = false;

    /**
     * 電文が完全な状態(すべてのデータを受信済み)であるかを表すフラグ<br>
     * 送信データは分割されている可能性もあるため(チェックポイント管理とは別に)、このフラグでデータの完全性を判定する
     */
    private boolean isComplete = false;

    /**
     * ユーザデータレコードの有無
     */
    private boolean isExistUserData = false;

    // -----------------------------------------------------
    // データ解析用定義
    // -----------------------------------------------------
    /**
     * メッセージ長開始オフセット
     */
    public static final int MSGLENGTH_OFFSET = 0;

    /**
     * メッセージ長レングス
     */
    public static final int MSGLENGTH_LENGTH = 8;

    /**
     * メッセージ種別開始オフセット
     */
    public static final int MSGTYPE_OFFSET = 8;

    /**
     * メッセージ種別レングス
     */
    public static final int MSGTYPE_LENGTH = 2;

    /**
     * 制御用レコード種別開始オフセット
     */
    public static final int CONTROLTYPE_OFFSET = 10;

    /**
     * 制御用レコード種別レングス
     */
    public static final int CONTROLTYPE_LENGTH = 3;

    /**
     * チェックポイント情報開始オフセット チェックポイント応答はソケットヘッダも含めるため開始オフセットは0
     */
    public static final int CHECKPOINTINFO_OFFSET = 0;

    /**
     * チェックポイント情報レングス
     */
    public static final int CHECKPOINTINFO_LENGTH = 30;

    /**
     * チェックポイント応答電文に付与するヘッダー部分の長さ
     */
    public static final int CHECKPOINTINFO_HEADER_LENGTH = 13;

    /**
     * ユーザデータ開始オフセット
     */
    public static final int USERDATA_OFFSET = 10;

    /**
     * メッセージ種別 ユーザデータ - 文字データ チェックポイント確認なし
     */
    public static final String MSGTYPE_AN = "AN";

    /**
     * メッセージ種別 ユーザデータ - バイナリデータ チェックポイント確認なし
     */
    public static final String MSGTYPE_BI = "BI";

    /**
     * メッセージ種別 ユーザデータ - FAX図 チェックポイント確認なし
     */
    public static final String MSGTYPE_FX = "FX";

    /**
     * メッセージ種別 ユーザデータ - 制御データ チェックポイント確認なし ※当メッセージ種別はJ-Alertからのみ受信
     */
    public static final String MSGTYPE_JL = "JL";

    /**
     * メッセージ種別 ユーザデータ - 制御データ チェックポイント確認なし
     */
    public static final String MSGTYPE_EN = "EN";

    /**
     * メッセージ種別 ユーザデータ - 文字データ チェックポイント確認あり
     */
    public static final String MSGTYPE_aN = "aN";

    /**
     * メッセージ種別 ユーザデータ - バイナリデータ チェックポイント確認あり
     */
    public static final String MSGTYPE_bI = "bI";

    /**
     * メッセージ種別 ユーザデータ - FAX図 チェックポイント確認あり
     */
    public static final String MSGTYPE_fX = "fX";

    /**
     * メッセージ種別 ユーザデータ - チェックポイント確認あり ※当メッセージ種別はJ-Alertからのみ受信
     */
    public static final String MSGTYPE_jL = "jL";

    /**
     * 制御レコード種別 チェックポイント通知
     */
    public static final String CONTROLTYPE_ACK = "ACK";

    /**
     * 制御レコード種別 ヘルスチェック(確認要求)
     */
    public static final String CONTROLTYPE_CHK_REQUEST = "chk";

    /**
     * ヘルスチェック確認回答用電文定義
     * 
     */
    public static final String RESPONSE_CHK = "00000003ENCHK";

    /**
     * 文字コード
     */
    private static final String CHARSET_UTF8 = "UTF-8";

    /**
     * コンストラクタ メッセージインスタンスを作成
     */
    public JmaMessage(byte[] data) {

        // パラメータチェック
        if (data == null) {
            IllegalArgumentException e = new IllegalArgumentException(
                    "データがnullです。");
            throw e;
        }
        // ソケットヘッダーよりデータ長が短い場合はエラー
        if (data.length < 10) {
            IllegalArgumentException e = new IllegalArgumentException(
                    "データ長が不正です。");
            throw e;
        }

        // 生データ保存
        this.data = data;

        // メッセージ長取得
        try {
            this.messageLength = Integer.valueOf(
                    new String(data, MSGLENGTH_OFFSET, MSGLENGTH_LENGTH,
                            CHARSET_UTF8)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            IllegalArgumentException ie = new IllegalArgumentException(
                    "メッセージ長部分が不正です。");
            throw ie;
        }

        // 電文ヘッダに記されているメッセージレングスと実際のユーザデータレングスが異なる場合は
        // データ完全性フラグをOFF
        if (getUserDataLength() != getMessageLength()) {
            this.isComplete = false;
        } else {
            this.isComplete = true;
        }

        // メッセージタイプ取得
        try {
            this.messageType = new String(data, MSGTYPE_OFFSET, MSGTYPE_LENGTH,
                    CHARSET_UTF8);
        } catch (Exception e) {
            e.printStackTrace();
            IllegalArgumentException ie = new IllegalArgumentException(
                    "メッセージタイプ部分が不正です。");
            throw ie;
        }

        // メッセージタイプにより振り分け
        if (this.messageType.equals(MSGTYPE_EN)) {
            // ---------------------------------
            // 制御用レコード
            // ---------------------------------
            // 制御レコード種別取得(受信側はヘルスチェックのみのはず)
            try {
                this.controlType = new String(data, CONTROLTYPE_OFFSET,
                        CONTROLTYPE_LENGTH, CHARSET_UTF8);
            } catch (Exception e) {
                e.printStackTrace();
                IllegalArgumentException ie = new IllegalArgumentException(
                        "制御レコード種別部分が不正です。");
                throw ie;
            }

            // ヘルスチェック要求の場合はOKだがそれ以外の制御コードは来ないはず
            if (this.controlType.equals(CONTROLTYPE_CHK_REQUEST)) {
                this.isHealthCheck = true;
            } else {
                IllegalArgumentException ie = new IllegalArgumentException(
                        "制御レコード種別が不正です。");
                throw ie;
            }
        } else {
            // ---------------------------------
            // ユーザデータレコードの場合
            // データが存在するというフラグを設定
            // データ自体をインスタンス変数として保持するとdata変数と重複してしまうので
            // getterメソッド内で都度データ作成
            // ---------------------------------
            this.isExistUserData = true;

            // 制御レコードがチェックポイントありの場合はフラグON
            if (this.messageType.equals(MSGTYPE_aN)
                    || this.messageType.equals(MSGTYPE_bI)
                    || this.messageType.equals(MSGTYPE_fX)
                    || this.messageType.equals(MSGTYPE_jL)) {
                // ---------------------------------
                // チェックポイント通知要求ありのメッセージ種別
                // ---------------------------------
                this.isCheckPoint = true;
            }

        }
    }

    /**
     * 現在保持しているデータに引数データを結合する.<br>
     * チェックポイントではなくてもデータが分割されて送られてくることがあるためこのメソッドでデータを結合する<br>
     * 
     * @param appendData
     * @return boolean データを結合
     */
    public boolean appendData(byte[] appendData) {

        // 配列を連結するためのロジック(参考)
        // T[] concat(T[] A, T[] B) {
        // T[] C= new T[A.length+B.length];
        // System.arraycopy(A, 0, C, 0, A.length);
        // System.arraycopy(B, 0, C, A.length, B.length);
        // return C;
        // }

        // 結合データ作成
        byte[] newData = new byte[data.length + appendData.length];
        System.arraycopy(this.data, 0, newData, 0, this.data.length);
        System.arraycopy(appendData, 0, newData, this.data.length,
                appendData.length);

        // 結合後データを保持
        this.data = newData;

        // 電文ヘッダに記されているメッセージレングスと実際のユーザデータレングスが一致したらデータ完全性フラグON
        if (getUserDataLength() == getMessageLength()) {
            this.isComplete = true;
            return true;
        } else if (getUserDataLength() > getMessageLength()){
            // データ長がヘッダの示すものより大きくなっている場合は結果falseをリターン
            this.isComplete = false;
            return false;
        } else {
            this.isComplete = false;
            return true;
        }
    }

    /**
     * メッセージの文字列表現
     */
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append("\n----------------------------------------------------------------------\n")
                .append("\tJMAソケットデータ\n").append(this.getClass().getName());
        sb.append("  メッセージ長\t[").append(this.messageLength).append("]\n");
        sb.append("  メッセージ種別\t[").append(this.messageType).append("]\n");
        if (this.messageType.equals(MSGTYPE_EN)) {
            sb.append("制御レコード種別\t[").append(this.controlType).append("]\n");
        }
        sb.append("----------------------------------------------------------------------\n");

        return sb.toString();
    }

    /**
     * ヘルスチェック電文を確認
     * 
     * @return true:ヘルスチェック電文 false:ヘルスチェック電文ではない
     */
    public boolean isHealthCheck() {
        return this.isHealthCheck;
    }

    /**
     * チェックポイント電文を確認
     * 
     * @return true:チェックポイント電文 false:チェックポイント電文ではない
     */
    public boolean isCheckPoint() {
        return this.isCheckPoint;
    }

    /**
     * データの完全性を確認<br>
     * データ長がヘッダーのデータ長と同じであればtrue,異なる場合はfalseを返却
     * 
     * @return true:データが全て揃っているとみなせる false:データはまだ揃っていないとみなせる
     */
    public boolean isComplete() {
        return this.isComplete;
    }

    /**
     * 生データをすべて取得
     * 
     * @return 生データ
     */
    public byte[] getData() {
        return data;
    }

    /**
     * メッセージ長を取得
     * 
     * @return int メッセージ長
     */
    public int getMessageLength() {
        return messageLength;
    }

    /**
     * メッセージ種別を取得
     * 
     * @return String メッセージ種別
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * 制御種別を取得
     * 
     * @return String 制御種別
     */
    public String getControlType() {
        return controlType;
    }

    /**
     * ヘルスチェックの応答データを作成して返却します。
     * 
     * @return byte[] ヘルスチェック応答データ
     */
    public static byte[] generateHelthcheckAck() {
        try {
            return JmaMessage.RESPONSE_CHK.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * チェックポイント応答電文を取得
     * 
     * @return String チェックポイント応答電文
     */
    public byte[] generateCheckPointAck() {

        // チェックポイント折り返し電文用byte[]作成
        int checkPointLength = 0;
        if (data.length < 30) {
            checkPointLength = CHECKPOINTINFO_HEADER_LENGTH + data.length;
        } else {
            checkPointLength = CHECKPOINTINFO_HEADER_LENGTH
                    + CHECKPOINTINFO_LENGTH;
        }
        byte[] ret = new byte[checkPointLength];

        // ヘッダーのレングス部分文字列作成
        String strLength = Integer.toString(checkPointLength);
        int l = strLength.length();
        for (int i = 0; i < 8 - l; i++) {
            strLength = "0" + strLength;
        }

        try {
            // retの先頭に設定
            System.arraycopy(strLength.getBytes(CHARSET_UTF8), 0, ret, 0, 8);
            // "ENACK"設定
            System.arraycopy("ENACK".getBytes(CHARSET_UTF8), 0, ret, 8, 5);
            // ユーザデータの先頭を設定
            System.arraycopy(data, 0, ret, 13, checkPointLength - 13);
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        return ret;
    }

    /**
     * データ全体からヘッダ部分を差し引いた部分(ユーザデータ部)レングスを返却
     * 
     * @return ユーザデータ部のレングス
     */
    public int getUserDataLength() {
        return data.length - USERDATA_OFFSET;
    }

    /**
     * ユーザデータ部をバイトデータ形式で取得
     * 
     * @return byte[] ユーザデータ
     */
    public byte[] getUserData() {
        if (this.isExistUserData) {
            // ヘッダー部分以外はすべてユーザデータ
            return Arrays.copyOfRange(data, USERDATA_OFFSET, data.length);
        } else {
            return null;
        }
    }

    /**
     * メッセージ種別がJMA仕様に則っているかを確認
     * @return
     */
    public boolean isValidMessageType() {
        if (this.messageType.equals(MSGTYPE_AN)
                || this.messageType.equals(MSGTYPE_BI)
                || this.messageType.equals(MSGTYPE_EN)
                || this.messageType.equals(MSGTYPE_FX)
                || this.messageType.equals(MSGTYPE_JL)
                || this.messageType.equals(MSGTYPE_aN)
                || this.messageType.equals(MSGTYPE_bI)
                || this.messageType.equals(MSGTYPE_fX)
                || this.messageType.equals(MSGTYPE_jL)) {
            return true;
        }
        return false;
    }

}
