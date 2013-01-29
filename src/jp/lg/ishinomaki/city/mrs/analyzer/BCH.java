//
//  BCH.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.analyzer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.lg.ishinomaki.city.mrs.receiver.ReceiverConfig;

/**
 * 電文制御ヘッダー(BCH)インスタンス<br>
 * BCHの先頭からBCHFormat.propertiesに定義されている定義名称とレングスで値を取得してインスタンス変数にMap形式で保持します。
 * 各値を取得する場合は各種getterを使用してください。
 * 
 */
public class BCH {

    /**
     * BCHのバイト配列
     */
    private byte[] byteBch = null;

    /**
     * BCHのビット符号文字列表現
     */
    private String strBch = null;

    /**
     * BCHの内容を保持するテーブル<br>
     * key:データを取り出すための識別子(String)、値にデータ内容を保持する
     */
    private Map<String, String> bchMap;

    // 以下、bchMapからデータを取得するための識別子
    //

    /**
     * バージョンNO
     */
    public final static String KEY_VERSIONNO = "versionNo";

    /**
     * 情報サイズ
     */
    public final static String KEY_BCHLENGTH = "bchLength";

    /**
     * 予備1
     */
    public final static String KEY_RESERVE1 = "reserve1";

    /**
     * 電文順序番号
     */
    public final static String KEY_SEQUENCENO = "sequenceNo";

    /**
     * 中継種別
     */
    public final static String KEY_RELAYTYPE = "relayType";

    /**
     * 地震フラグ
     */
    public final static String KEY_EMERGENCYTYPE = "emergencyType";

    /**
     * 予備2
     */
    public final static String KEY_RESERVE2 = "reserve2";

    /**
     * テストフラグ
     */
    public final static String KEY_TESTTYPE = "testType";

    /**
     * XMLフラグ
     */
    public final static String KEY_XMLTYPE = "xmlType";

    /**
     * データ機密度
     */
    public final static String KEY_CLASSIFICATIONLEVEL = "classificationLevel";

    /**
     * データ属性
     */
    public final static String KEY_DATAATTRIBUTE = "dataAttribute";

    /**
     * 気象庁内配信情報
     */
    public final static String KEY_AGENCY = "agency";

    /**
     * データ種別
     */
    public final static String KEY_DATATYPE = "dataType";

    /**
     * 未使用
     */
    public final static String KEY_RESERVE3 = "reserve3";

    /**
     * 電文情報(BIF) 再送フラグ
     */
    public final static String KEY_BIF_RESENDTYPE = "bifResendType";

    /**
     * 電文情報(BIF) データ属性
     */
    public final static String KEY_BIF_DATAATTRIBUTE = "bifDataAttribute";

    /**
     * 電文情報(BIF) データ種別
     */
    public final static String KEY_BIF_DATATYPE = "bifDataType";

    /**
     * A/N桁数
     */
    public final static String KEY_ANLENGTH = "anLength";

    /**
     * QCチェックサム
     */
    public final static String KEY_QCCHECKSUM = "qcChecksum";

    /**
     * 発信官署番号 - 大分類
     */
    public final static String KEY_SENDNO_CLASSIFICATION = "sendNoClassification";

    /**
     * 発信官署番号 - 該当システム・ビット
     */
    public final static String KEY_SENDNO_IDENTIFIER = "sendNoIdentifier";

    /**
     * 発信官署番号 - 各システムの管理する端末の番号
     */
    public final static String KEY_SENDNO_TERMINAL = "sendNoTerminal";

    /**
     * 着信官署番号 - 大分類
     */
    public final static String KEY_RECEIVENO_CLASSIFICATION = "receiveNoClassification";

    /**
     * 着信官署番号 - 該当システム・ビット
     */
    public final static String KEY_RECEIVENO_IDENTIFIER = "receiveNoIdentifier";

    /**
     * 着信官署番号 - 各システムの管理する端末の番号
     */
    public final static String KEY_RECEIVENO_TERMINAL = "receiveNoTerminal";

    // 以下、ヘッダーの定義内容を表す

    /**
     * 中継種別フラグ 通常の中継
     */
    public final static int RELAYTYPE_NORMAL = 0;

    /**
     * 中継種別フラグ 着信官署端末番号による単純中継
     */
    public final static int RELAYTYPE_ABNORMAL = 1;

    /**
     * 地震・津波報フラグ 通常電文
     */
    public final static int EMERGENCYTYPE_OFF = 0;

    /**
     * 地震・津波報フラグ 地震・津波電文
     */
    public final static int EMERGENCYTYPE_ON = 1;

    /**
     * テストフラグ 通常電文
     */
    public final static int TESTTYPE_OFF = 0;

    /**
     * テストフラグ テスト電文
     */
    public final static int TESTTYPE_ON = 1;

    /**
     * XMLフラグ 気象庁防災情報XML電文以外の電文
     */
    public final static int XMLTYPE_NOXML = 0;

    /**
     * XMLフラグ 気象庁防災情報XML電文であり、圧縮なし
     */
    public final static int XMLTYPE_XML = 1;

    /**
     * XMLフラグ 気象庁防災情報XML電文であり、gzip圧縮
     */
    public final static int XMLTYPE_XML_ON_GZIP = 2;

    /**
     * XMLフラグ 気象庁防災情報XML電文であり、zip圧縮
     */
    public final static int XMLTYPE_XML_ON_ZIP = 3;

    /**
     * データ属性 未使用
     */
    public final static int DATAATTRIBUTE_NA = 0;
    /**
     * データ属性 1バイト系
     */
    public final static int DATAATTRIBUTE_1BYTE = 1;
    /**
     * データ属性 漢字(シフトJIS)
     */
    public final static int DATAATTRIBUTE_SHIFTJIS = 2;
    /**
     * データ属性 漢字(JIS)
     */
    public final static int DATAATTRIBUTE_JIS = 3;
    /**
     * データ属性 予備
     */
    public final static int DATAATTRIBUTE_RESERVE = 4;
    /**
     * データ属性 バイナリ
     */
    public final static int DATAATTRIBUTE_BINARY = 5;

    /**
     * データ種別大分類 運用報
     */
    public final static int MAJORDATATYPE_MESSAGE = 0;
    public final static String MAJORDATATYPE_MESSAGE_STRING = "運用報";

    /**
     * データ種別大分類 地震・津波報
     */
    public final static int MAJORDATATYPE_EMERGENCY = 1;
    public final static String MAJORDATATYPE_EMERGENCY_STRING = "地震・津波報";

    /**
     * データ種別大分類 国内発信通常報
     */
    public final static int MAJORDATATYPE_INTERNAL = 2;
    public final static String MAJORDATATYPE_INTERNAL_STRING = "国内発信通常報";

    /**
     * データ種別大分類 国外発信通常報
     */
    public final static int MAJORDATATYPE_INTERNATIONAL = 3;
    public final static String MAJORDATATYPE_INTERNATIONAL_STRING = "国外発信通常報";

    /**
     * データ種別大分類 国内発信バイナリ
     */
    public final static int MAJORDATATYPE_INTERNAL_BINARY = 4;
    public final static String MAJORDATATYPE_INTERNAL_BINARY_STRING = "国内発信バイナリ";

    /**
     * データ種別大分類 国外発信バイナリ
     */
    public final static int MAJORDATATYPE_INTERNATIONAL_BINARY = 6;
    public final static String MAJORDATATYPE_INTERNATIONAL_BINARY_STRING = "国外発信バイナリ";

    /**
     * データ種別大分類 エラー返信報
     */
    public final static int MAJORDATATYPE_ERROR = 15;
    public final static String MAJORDATATYPE_ERROR_STRING = "エラー返信報";

    /**
     * BIF 再送フラグ 再送電文以外
     */
    public final static int BIFRESEND_NO = 0;

    /**
     * BIF 再送フラグ 再送電文
     */
    public final static int BIFRESEND = 1;

    /**
     * BIF データ属性 1バイト系
     */
    public final static int BIFDATAATTR_1BYTE_0 = 0;
    /**
     * BIF データ属性 1バイト系
     */
    public final static int BIFDATAATTR_1BYTE_1 = 1;
    /**
     * BIF データ属性 漢字(シフトJIS)
     */
    public final static int BIFDATAATTR_SHIFTJIS = 2;
    /**
     * BIF データ属性 バイナリ
     */
    public final static int BIFDATAATTR_BINARY = 3;

    /**
     * BIF データ種別 運用報
     */
    public final static int BIFDATATYPE_UNYO = 0;
    /**
     * BIF データ種別 津波報
     */
    public final static int BIFDATATYPE_TSUNAMI = 1;
    /**
     * BIF データ種別 ケンソク報
     */
    public final static int BIFDATATYPE_KENSOKU = 2;
    /**
     * BIF データ種別 シンゲン報
     */
    public final static int BIFDATATYPE_SINGEN = 3;
    /**
     * BIF データ種別 ヒジョウ報
     */
    public final static int BIFDATATYPE_HIJYOU = 4;
    /**
     * BIF データ種別 ジシン報
     */
    public final static int BIFDATATYPE_JISIN = 5;
    /**
     * BIF データ種別 一般気象報
     */
    public final static int BIFDATATYPE_KISHOU = 6;
    /**
     * BIF データ種別 CDF報
     */
    public final static int BIFDATATYPE_CDF = 7;
    /**
     * BIF データ種別 SGT報
     */
    public final static int BIFDATATYPE_SGT = 8;
    /**
     * BIF データ種別 DRE報
     */
    public final static int BIFDATATYPE_DRE = 9;
    /**
     * BIF データ種別 メール報
     */
    public final static int BIFDATATYPE_MAIL = 10;
    /**
     * BIF データ種別 バッチ系バイナリ報
     */
    public final static int BIFDATATYPE_BATCH = 11;
    /**
     * BIF データ種別 GTS系バイナリ報
     */
    public final static int BIFDATATYPE_GTS = 12;
    /**
     * BIF データ種別 国内バイナリ報
     */
    public final static int BIFDATATYPE_KOKUNAI = 13;
    /**
     * BIF データ種別 降水強度指数データ
     */
    public final static int BIFDATATYPE_KOSUI = 14;
    /**
     * BIF データ種別 その他データ
     */
    public final static int BIFDATATYPE_SONOTA = 15;

    /**
     * コンストラクタです。<br>
     * BCHを含む本文情報を指定してください。 BCH部分を判断し必要な情報を当インスタンスに保持します。
     */
    public BCH(byte[] data) {

        // テーブル初期化
        bchMap = new HashMap<String, String>();

        // 引数データから先頭20オクテットを取得(この部分がBCHの領域)
        this.byteBch = Arrays.copyOf(data, 20);

        // byte[]を2進数文字列に変換
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteBch.length; i++) {
            // byte 値 b を、符号拡張なしで
            // charへ変換したい場合には、符号拡張を行わないようにするために、ビットマスクを使用しなければなりません。
            int c = byteBch[i] & 0xff;
            sb.append(fullZero(Integer.toBinaryString(c), 8));
        }
        this.strBch = sb.toString();

        // format定義に従って分割しMapに保存
        List<Map<String, Integer>> divider = (List<Map<String, Integer>>) ReceiverConfig
                .getInstance().getBch_divider();

        int cursor = 0;
        for (Map<String, Integer> map : divider) {
            // フォーマットプロパティからキー名称と対応するレングス取得
            String key = map.keySet().iterator().next();
            Integer length = map.get(key);

            // BCH全体の文字列から該当部分を取り出してMapに保存
            // このときのキー名称はフォーマットプロパティのものを使用する
            int endoffset = cursor + length.intValue();
            String val = strBch.substring(cursor, endoffset);
            bchMap.put(key, val);

            // カーソル位置更新
            cursor = endoffset;
        }
    }

    /**
     * BCHのbyte配列表現を取得します。
     * 
     * @return byte[] BCH内容
     */
    public byte[] getByteBch() {
        return this.byteBch;
    }

    /**
     * BCHの文字列表現を取得します。
     * 
     * @return String BCH内容
     */
    public String getStrBch() {
        return this.strBch;
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

    /**
     * メッセージの文字列表現
     */
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append("\n----------------------------------------------------------------------\n")
                .append("\t電文制御ヘッダー(BCH)\n\n");
        sb.append("バージョン番号\t\t[").append(this.getVersionNo()).append("]\n");
        sb.append("BCHレングス\t\t[").append(this.getBchLength()).append("]\n");
        sb.append("電文順序番号\t\t[").append(this.getSequenceNo()).append("]\n");
        sb.append("処理情報(PIF)\n");

        int relayType = this.getRelayType();
        sb.append("  中継種別\t\t[" + relayType + "] ");
        switch (relayType) {
        case RELAYTYPE_NORMAL:
            sb.append("通常の中継\n");
            break;
        case RELAYTYPE_ABNORMAL:
            sb.append("着信官署端末番号による単純中継\n");
            break;
        default:
            sb.append("\n");
        }

        int emergencyType = this.getEmergencyType();
        sb.append("  地震フラグ\t\t[" + emergencyType + "] ");
        switch (emergencyType) {
        case EMERGENCYTYPE_OFF:
            sb.append("通常電文\n");
            break;
        case EMERGENCYTYPE_ON:
            sb.append("地震・津波電文\n");
            break;
        default:
            sb.append("\n");
        }

        int testType = this.getTestType();
        sb.append("  テストフラグ\t\t[" + testType + "] ");
        switch (testType) {
        case TESTTYPE_OFF:
            sb.append("通常電文\n");
            break;
        case TESTTYPE_ON:
            sb.append("テスト電文\n");
            break;
        default:
            sb.append("\n");
        }

        int xmlType = this.getXmlType();
        sb.append("  XMLフラグ\t\t[" + xmlType + "] ");
        switch (xmlType) {
        case XMLTYPE_NOXML:
            sb.append("XML電文以外の電文\n");
            break;
        case XMLTYPE_XML:
            sb.append("XML電文であり、圧縮なし\n");
            break;
        case XMLTYPE_XML_ON_GZIP:
            sb.append("XML電文であり、gzip圧縮\n");
            break;
        case XMLTYPE_XML_ON_ZIP:
            sb.append("XML電文であり、zip圧縮\n");
            break;
        default:
            sb.append("\n");
        }

        int dataAttr = this.getDataAttribute();
        sb.append("  データ属性\t\t[" + dataAttr + "] ");
        switch (dataAttr) {
        case DATAATTRIBUTE_NA:
            sb.append("未使用\n");
            break;
        case DATAATTRIBUTE_1BYTE:
            sb.append("1バイト系\n");
            break;
        case DATAATTRIBUTE_SHIFTJIS:
            sb.append("漢字(シフトJIS)\n");
            break;
        case DATAATTRIBUTE_JIS:
            sb.append("漢字(JIS)\n");
            break;
        case DATAATTRIBUTE_RESERVE:
            sb.append("予備\n");
            break;
        case DATAATTRIBUTE_BINARY:
            sb.append("バイナリ\n");
            break;
        default:
            sb.append("\n");
        }

        int majorDataType = this.getMajorDataType();
        int minorDataType = this.getMinorDataType();
        sb.append("  データ種別(大分類)\t[" + majorDataType + "] ");
        switch (majorDataType) {
        case MAJORDATATYPE_MESSAGE:
            sb.append(MAJORDATATYPE_MESSAGE_STRING).append("\n");
            sb.append("  データ種別(小分類)\t[" + minorDataType + "] ");
            switch (minorDataType) {
            case 0:
                sb.append("小分類の設定なし\n");
                break;
            case 1:
                sb.append("指示メッセージ報(メッセージ番号なし)\n");
                break;
            case 2:
                sb.append("指示メッセージ報(メッセージ番号あり)\n");
                break;
            case 3:
                sb.append("指示メッセージ回答報\n");
                break;
            default:
                sb.append("\n");
            }
            break;
        case MAJORDATATYPE_EMERGENCY:
            sb.append(MAJORDATATYPE_EMERGENCY_STRING).append("\n");
            ;
            sb.append("  データ種別(小分類)\t[" + minorDataType + "] ");
            switch (minorDataType) {
            case 0:
                sb.append("小分類の設定なし\n");
                break;
            case 1:
                sb.append("ツナミ報\n");
                break;
            case 2:
                sb.append("ケンソク報\n");
                break;
            case 3:
                sb.append("ヒジョウ報\n");
                break;
            case 4:
                sb.append("シンゲン報\n");
                break;
            case 5:
                sb.append("エンチツナミ報\n");
                break;
            case 6:
                sb.append("ハンテイカイ報\n");
                break;
            case 7:
                sb.append("ジシンヨチ報\n");
                break;
            case 8:
                sb.append("クンレン報\n");
                break;
            default:
                sb.append("\n");
            }
            break;
        case MAJORDATATYPE_INTERNAL:
            sb.append(MAJORDATATYPE_INTERNAL_STRING).append("\n");
            sb.append("  データ種別(小分類)\t[" + minorDataType + "] ");
            switch (minorDataType) {
            case 0:
                sb.append("小分類の設定なし\n");
                break;
            case 1:
                sb.append("観測報\n");
                break;
            case 2:
                sb.append("注・警報\n");
                break;
            case 3:
                sb.append("予報\n");
                break;
            case 4:
                sb.append("指示報\n");
                break;
            case 5:
                sb.append("統計用報告報\n");
                break;
            case 6:
                sb.append("事務報・連絡報\n");
                break;
            default:
                sb.append("\n");
            }
            break;
        case MAJORDATATYPE_INTERNATIONAL:
            sb.append(MAJORDATATYPE_INTERNATIONAL_STRING).append(" \n");
            sb.append("  データ種別(小分類)\t[" + minorDataType + "] ");
            switch (minorDataType) {
            case 0:
                sb.append("小分類の設定なし\n");
                break;
            case 1:
                sb.append("観測報\n");
                break;
            case 6:
                sb.append("管理法・サービスメッセージ\n");
                break;
            case 7:
                sb.append("リクエスト報\n");
                break;
            case 8:
                sb.append("データメッセージ\n");
                break;
            default:
                sb.append("\n");
            }
            break;
        case MAJORDATATYPE_INTERNAL_BINARY:
            sb.append(MAJORDATATYPE_INTERNAL_BINARY_STRING).append(" \n");
            sb.append("  データ種別(小分類)\t[" + minorDataType + "] ");
            switch (minorDataType) {
            case 0:
                sb.append("小分類の設定なし\n");
                break;
            case 1:
                sb.append("NAPS作成GPV\n");
                break;
            case 2:
                sb.append("地方作成GPV\n");
                break;
            case 3:
                sb.append("BUFR報\n");
                break;
            case 4:
                sb.append("GRIB報\n");
                break;
            case 5:
                sb.append("FAX\n");
                break;
            case 6:
                sb.append("気象庁方式デコード\n");
                break;
            case 7:
                sb.append("衛星画像報\n");
                break;
            case 8:
                sb.append("デジタル・レコーダーエコー\n");
                break;
            case 9:
                sb.append("監視レコーダーエコー\n");
                break;
            case 10:
                sb.append("気象庁方式メール報\n");
                break;
            case 11:
                sb.append("気象庁方式ファイル転送報\n");
                break;
            case 12:
                sb.append("地震波形報\n");
                break;
            case 13:
                sb.append("降水強度指数データ\n");
                break;
            default:
                sb.append("\n");
            }
            break;
        case MAJORDATATYPE_INTERNATIONAL_BINARY:
            sb.append(MAJORDATATYPE_INTERNATIONAL_BINARY_STRING).append(" \n");
            sb.append("  データ種別(小分類)\t[" + minorDataType + "] ");
            switch (minorDataType) {
            case 0:
                sb.append("小分類の設定なし\n");
                break;
            case 3:
                sb.append("BUFR報\n");
                break;
            case 4:
                sb.append("GRIB報\n");
                break;
            case 5:
                sb.append("FAX\n");
                break;
            case 12:
                sb.append("地震波形報\n");
                break;
            default:
                sb.append("\n");
                break;
            }
            break;
        case MAJORDATATYPE_ERROR:
            sb.append(MAJORDATATYPE_ERROR_STRING).append(" \n");
            sb.append("  データ種別(小分類)\t[" + minorDataType + "] ");
            switch (minorDataType) {
            case 0:
                sb.append("小分類の設定なし\n");
                break;
            default:
                sb.append("\n");
                break;
            }
            break;
        default:
        }

        sb.append("電文情報(BIF)\n");

        int bifResendType = this.getBifResendType();
        sb.append("  再送フラグ\t\t[" + bifResendType + "] ");
        switch (bifResendType) {
        case BIFRESEND_NO:
            sb.append("再送電文以外\n");
            break;
        case BIFRESEND:
            sb.append("再送電文\n");
            break;
        default:
            sb.append("\n");
            break;
        }

        int bifDataAttr = this.getBifDataAttribute();
        sb.append("  データ属性\t\t[" + bifDataAttr + "] ");
        switch (bifDataAttr) {
        case BIFDATAATTR_1BYTE_0:
            sb.append("1バイト系\n");
            break;
        case BIFDATAATTR_1BYTE_1:
            sb.append("1バイト系\n");
            break;
        case BIFDATAATTR_SHIFTJIS:
            sb.append("漢字(シフトJIS)\n");
            break;
        case BIFDATAATTR_BINARY:
            sb.append("バイナリ\n");
            break;
        default:
            sb.append("\n");
        }

        int bifDataType = this.getBifDataType();
        sb.append("  データ種別\t\t[" + bifDataType + "] ");
        switch (bifDataType) {
        case BIFDATATYPE_UNYO:
            sb.append("運用報\n");
            break;
        case BIFDATATYPE_TSUNAMI:
            sb.append("津波報\n");
            break;
        case BIFDATATYPE_KENSOKU:
            sb.append("ケンソク報\n");
            break;
        case BIFDATATYPE_SINGEN:
            sb.append("シンゲン報\n");
            break;
        case BIFDATATYPE_HIJYOU:
            sb.append("ヒジョウ報\n");
            break;
        case BIFDATATYPE_JISIN:
            sb.append("ジシン報\n");
            break;
        case BIFDATATYPE_KISHOU:
            sb.append("一般気象報\n");
            break;
        case BIFDATATYPE_CDF:
            sb.append("CDF報\n");
            break;
        case BIFDATATYPE_SGT:
            sb.append("SGT報\n");
            break;
        case BIFDATATYPE_DRE:
            sb.append("DRE報\n");
            break;
        case BIFDATATYPE_MAIL:
            sb.append("メール報\n");
            break;
        case BIFDATATYPE_BATCH:
            sb.append("バッチ系バイナリ報\n");
            break;
        case BIFDATATYPE_GTS:
            sb.append("GTS報\n");
            break;
        case BIFDATATYPE_KOKUNAI:
            sb.append("屋内バイナリ報\n");
            break;
        case BIFDATATYPE_KOSUI:
            sb.append("降水強度指数データ\n");
            break;
        case BIFDATATYPE_SONOTA:
            sb.append("その他\n");
            break;
        default:
            break;
        }

        sb.append("  A/N桁数\t\t[").append(this.getAnLength()).append("]\n");
        sb.append("  QCチェックサム\t\t[").append(this.getQcChecksum()).append("]\n");

        sb.append("発信官署番号\n");
        sb.append("  大分類\t\t\t[").append(this.getSendNoClassification())
                .append("]\n");
        sb.append("  該当システムビット\t[").append(this.getSendNoIdentifier())
                .append("]\n");
        sb.append("  端末番号\t\t[").append(this.getSendNoTerminal()).append("]\n");
        sb.append("着信官署番号\n");
        sb.append("  大分類\t\t\t[").append(this.getReceiveNoClassification())
                .append("]\n");
        sb.append("  該当システムビット\t[").append(this.getReceiveNoIdentifier())
                .append("]\n");
        sb.append("  端末番号\t\t[").append(this.getReceiveNoTerminal())
                .append("]\n");
        sb.append("----------------------------------------------------------------------\n");

        return sb.toString();
    }

    /**
     * BCH内容を保持したテーブルを取得します。
     * 
     * @return Map BCH保持テーブル
     */
    public Map<String, String> getBchMap() {
        return bchMap;
    }

    /**
     * バージョン番号を取得します。
     * 
     * @return int バージョン番号
     */
    public int getVersionNo() {
        String str = bchMap.get(KEY_VERSIONNO);
        // 文字列表現(2進数)を数値に変換 "0100" -> 4
        return Integer.parseInt(str, 2);
    }

    /**
     * BCH全体の電文長を取得します。
     * 
     * @return int 電文長
     */
    public int getBchLength() {
        String str = bchMap.get(KEY_BCHLENGTH);
        // BCH仕様ではデータ長=1で4オクテットを表現するため4倍の値を返却
        return Integer.parseInt(str, 2) * 4;
    }

    /**
     * 電文順序番号を取得します。
     * 
     * @return int 電文順序番号
     */
    public int getSequenceNo() {
        String str = bchMap.get(KEY_SEQUENCENO);
        // 2進化10進数(4bitで1つの10進数数値)で5桁の数値を表現しているため数値に変換する
        StringBuilder sb = new StringBuilder();
        int begin_index = 0;
        int end_index = 4;
        // 文字列を4文字(4bit)づつ取得し10進数数値に変換し文字列としてsbに追加
        for (int i = 0; i < 5; i++) {
            String substr = str.substring(begin_index, end_index);
            // 2進化文字列をintに変換
            int bcd = Integer.parseInt(substr, 2);
            sb.append(String.valueOf(bcd));
            begin_index += 4;
            end_index += 4;
        }
        // 最後にsbの内容をまとめて10進数int型にして返却
        return Integer.parseInt(sb.toString());
    }

    /**
     * 中継種別フラグを取得します。
     * 
     * @return int 中継種別フラグ
     */
    public int getRelayType() {
        String str = bchMap.get(KEY_RELAYTYPE);
        return Integer.parseInt(str, 2);
    }

    /**
     * 地震・津波報フラグを取得します。
     * 
     * @return int 地震・津波報フラグ
     */
    public int getEmergencyType() {
        String str = bchMap.get(KEY_EMERGENCYTYPE);
        return Integer.parseInt(str, 2);
    }

    /**
     * テストフラグを取得します。
     * 
     * @return int テストフラグ
     */
    public int getTestType() {
        String str = bchMap.get(KEY_TESTTYPE);
        return Integer.parseInt(str, 2);
    }

    /**
     * XMLフラグを取得します。
     * 
     * @return int XMLフラグ
     */
    public int getXmlType() {
        String str = bchMap.get(KEY_XMLTYPE);
        return Integer.parseInt(str, 2);
    }

    /**
     * データ機密度フラグを取得します。
     * 
     * @return int データ機密度フラグ
     */
    public int getClassificationLevel() {
        String str = bchMap.get(KEY_CLASSIFICATIONLEVEL);
        return Integer.parseInt(str, 2);
    }

    /**
     * データ属性を取得します。
     * 
     * @return int データ属性
     */
    public int getDataAttribute() {
        String str = bchMap.get(KEY_DATAATTRIBUTE);
        return Integer.parseInt(str, 2);
    }

    /**
     * 気象庁内配信情報を取得します。
     * 
     * @return int 気象庁内配信情報
     */
    public int getAgency() {
        String str = bchMap.get(KEY_AGENCY);
        return Integer.parseInt(str, 2);
    }

    /**
     * データ種別(大分類)を取得します。
     * 
     * @return int データ種別(大分類)
     */
    public int getMajorDataType() {
        // 大分類はdataTypeのうち上位4ビット
        String str = bchMap.get(KEY_DATATYPE);
        String substr = str.substring(0, 4);
        return Integer.parseInt(substr, 2);
    }

    /**
     * データ種別(大分類)の文字列表現を取得します。
     * 
     * @return String データ種別(大分類)の文字列表現
     */
    public String getMajorDataTypeString() {
        int type = getMajorDataType();
        switch (type) {
        case MAJORDATATYPE_MESSAGE:
            return MAJORDATATYPE_MESSAGE_STRING;
        case MAJORDATATYPE_EMERGENCY:
            return MAJORDATATYPE_EMERGENCY_STRING;
        case MAJORDATATYPE_INTERNAL:
            return MAJORDATATYPE_INTERNAL_STRING;
        case MAJORDATATYPE_INTERNAL_BINARY:
            return MAJORDATATYPE_INTERNAL_BINARY_STRING;
        case MAJORDATATYPE_INTERNATIONAL:
            return MAJORDATATYPE_INTERNATIONAL_STRING;
        case MAJORDATATYPE_INTERNATIONAL_BINARY:
            return MAJORDATATYPE_INTERNATIONAL_BINARY_STRING;
        case MAJORDATATYPE_ERROR:
            return MAJORDATATYPE_ERROR_STRING;
        }
        return null;
    }

    /**
     * データ種別(小分類)を取得します。
     * 
     * @return int データ種別(小分類)
     */
    public int getMinorDataType() {
        // 小分類はdataTypeのうち下位4ビット
        String str = bchMap.get(KEY_DATATYPE);
        String substr = str.substring(4, 8);
        return Integer.parseInt(substr, 2);
    }

    /**
     * 電文情報[BIF]内の再送フラグを取得します。
     * 
     * @return int 再送フラグ
     */
    public int getBifResendType() {
        String str = bchMap.get(KEY_BIF_RESENDTYPE);
        return Integer.parseInt(str, 2);
    }

    /**
     * 電文情報[BIF]内のデータ属性フラグを取得します。
     * 
     * @return int データ属性
     */
    public int getBifDataAttribute() {
        String str = bchMap.get(KEY_BIF_DATAATTRIBUTE);
        return Integer.parseInt(str, 2);
    }

    /**
     * 電文情報[BIF]内のデータ種別を取得します。
     * 
     * @return int データ種別
     */
    public int getBifDataType() {
        String str = bchMap.get(KEY_BIF_DATATYPE);
        return Integer.parseInt(str, 2);
    }

    /**
     * A/N桁数を取得します。
     * 
     * @return int A/N桁数
     */
    public int getAnLength() {
        String str = bchMap.get(KEY_ANLENGTH);
        return Integer.parseInt(str, 2);
    }

    /**
     * QCチェックサムの値を取得します。
     * 
     * @return String QCチェックサム
     */
    public String getQcChecksum() {
        String str = bchMap.get(KEY_QCCHECKSUM);
        return str;
    }

    /**
     * 発信官署番号の大分類を取得します。
     * 
     * @return int 発信官署番号の大分類
     */
    public int getSendNoClassification() {
        String str = bchMap.get(KEY_SENDNO_CLASSIFICATION);
        return Integer.parseInt(str, 2);
    }

    /**
     * 発信官署番号のシステム識別フラグを取得します。 ここでは2オクテット分の2進数ビットを文字列で表現したものを返却します。
     * 
     * @return String 発信官署番号のシステム識別フラグ
     */
    public String getSendNoIdentifier() {
        String str = bchMap.get(KEY_SENDNO_IDENTIFIER);
        return str;
    }

    /**
     * 発信官署番号の端末番号を取得します。
     * 
     * @return 発信官署番号の端末番号
     */
    public String getSendNoTerminal() {
        String str = bchMap.get(KEY_SENDNO_TERMINAL);
        return str;
    }

    /**
     * 着信官署番号の大分類を取得します。
     * 
     * @return int 着信官署番号の大分類
     */
    public int getReceiveNoClassification() {
        String str = bchMap.get(KEY_RECEIVENO_CLASSIFICATION);
        return Integer.parseInt(str, 2);
    }

    /**
     * 着信官署番号のシステム識別フラグを取得します。 ここでは2オクテット分の2進数ビットを文字列で表現したものを返却します。
     * 
     * @return String 着信官署番号のシステム識別フラグ
     */
    public String getReceiveNoIdentifier() {
        String str = bchMap.get(KEY_RECEIVENO_IDENTIFIER);
        return str;
    }

    /**
     * 着信官署番号の端末番号を取得します。
     * 
     * @return 着信官署番号の端末番号
     */
    public String getReceiveNoTerminal() {
        String str = bchMap.get(KEY_RECEIVENO_TERMINAL);
        return str;
    }

}
