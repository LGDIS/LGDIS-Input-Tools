//
//  JAlertKishouDataAnalyzer.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.analyzer;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.Consts;
import jp.lg.ishinomaki.city.mrs.utils.BCHChecksumHelper;
import jp.lg.ishinomaki.city.mrs.utils.ZipHelper;

/**
 * J-Alertから受信する気象庁データの解析クラスです。<br>
 * データをBCH、電文ヘッディング、本文に分割します。<br>
 * 
 */
public class JmaDataAnalyzer implements DataAnalyzer {

    /**
     * 当クラスのロガーインスタンス
     */
    private final Logger log = Logger.getLogger(JmaDataAnalyzer.class
            .getSimpleName());

    /**
     * BCHインスタンス
     */
    private BCH bch = null;

    /**
     * 電文ヘディング内の"冒頭符"
     */
    private String headerCode = null;

    /**
     * 電文ヘディング内の発信官署名
     */
    private String senderSign = null;

    /**
     * 観測日時刻
     */
    private String observationDate = null;

    /**
     * 指定コード
     */
    private String appointCode = null;

    /**
     * 本文
     */
    private byte[] contents = null;

    /**
     * 電文ヘッディング内で使用する復帰改行(電文ヘディングと本文とのデリミタとして使用)
     */
    private static int NL = 0x0A;

    /**
     * 電文ヘッディング内で使用するテキスト開始符号
     */
    private static int STX = 0x02;

    /**
     * 電文ヘッディングで使用するスペース(電文ヘディング内のデリミタとして使用)
     */
    private static int SP = 0x20;

    /**
     * コンストラクタです。 特に処理はありません。
     */
    public JmaDataAnalyzer() {
        super();
    }

    /**
     * 引数で与えられた電文の内容を解析して内容を自インスタンスに保持します。<br>
     * 解析後のデータは各getterメソッドで取得してください。
     * 
     * @param data
     *            JMA通信で取得したユーザデータ部のbyte配列
     */
    @Override
    public void analyze(byte[] data) {

        // -----------------------------------
        // BCH作成
        // -----------------------------------
        bch = new BCH(data);
        log.finest(bch.toString()); // デバッグ用ログ

        // -----------------------------------
        // BCHのチェックサム
        // -----------------------------------
        boolean checksum = BCHChecksumHelper.check(bch.getStrBch(),
                bch.getQcChecksum());
        if (checksum == false) {

            // --------------------------------------
            // エラーログ出力
            // ログのフォーマットは詳細設計書に記載のもの
            // --------------------------------------
            log.severe("チェックサムエラー");
            log.severe("ただし処理は続行します"); // for test
            // TODO for test
            // チェックサムエラーでも後続処理を続行
            // throw new InvalidParameterException("チェックサムエラーのため処理を中断します。");
        }

        // -----------------------------------
        // データ属性により解析処理を行う
        // -----------------------------------
        // バイナリデータ
        // バイナリデータの判定をBCHの"PIF->データ属性"で行う
        if (this.bch.getDataAttribute() == BCH.DATAATTRIBUTE_BINARY) {
            log.finest("[データ属性]の設定がバイナリのためバイナリ用の解析");
            analyzeBinary(data);
            return;
        }

        // 非バイナリデータ
        else {
            log.finest("[データ属性]の設定がバイナリ以外のためバイナリ以外用の解析");
            analyzeText(data);
            return;
        }
    }

    /**
     * 内部メソッド バイナリ以外のデータ受信時の解析処理を実施
     * 
     * @param data
     *            ユーザデータ全体
     */
    private void analyzeText(final byte[] data) {

        // -----------------------------------
        // 電文ヘッディング部解析
        // パターン1のみ(パターン2は存在しない)
        // -----------------------------------
        byte[] heading = null;

        // データ種別が'バイナリ'でない場合はパターン1、'バイナリ'の場合はパターン3
        if (this.bch.getDataAttribute() != 5) {
            // パターン1
            // BCH終了後から'NL'〜'NL'までを電文ヘディングとして認識
            int beginNl = 0;
            int endNl = 0;
            // 'NL'開始位置取得
            for (int i = bch.getBchLength(); i < data.length
                    - bch.getBchLength(); i++) {
                byte abyte = data[i];
                if (abyte == NL) {
                    beginNl = i;
                    break;
                }
            }
            // 'NL'終了位置取得
            for (int i = beginNl + 1; i < data.length - (beginNl + 1); i++) {
                byte abyte = data[i];
                if (abyte == NL) {
                    endNl = i;
                    break;
                }
            }
            // 電文ヘディング部取得
            heading = Arrays.copyOfRange(data, beginNl + 1, endNl);

        } else {
        }

        // 電文ヘディング部解析
        this.analyzeHeading(heading);

        // -----------------------------------
        // 本文部抽出
        // パターン1(テキスト本文)とパターン3(バイナリ本文)のみ考慮
        // -----------------------------------
        // 本文部分を抽出
        byte[] work_contents = Arrays.copyOfRange(data, heading.length,
                data.length);

        // データ種別が'バイナリ'でない場合はパターン1、'バイナリ'の場合はパターン3
        if (this.bch.getDataAttribute() != 5) {
            // パターン1の場合は本文符号NLを除いた部分を本文とする
            int beginNl = 0;
            int endNl = 0;
            for (int i = 0; i < work_contents.length; i++) {
                byte abyte = work_contents[i];
                if (abyte != NL && abyte != STX) {
                    beginNl = i;
                    break;
                }
            }

            for (int i = beginNl + 1; i < work_contents.length; i++) {
                byte abyte = work_contents[i];
                if (abyte == NL) {
                    endNl = i;
                    break;
                }
            }
            // 本文(NL,STX,ETXを除いた部分)を抽出
            this.contents = Arrays.copyOfRange(work_contents, beginNl, endNl);
        } else {
            // パターン2(バイナリ)の場合は電文ヘディング以外はすべて本文
            this.contents = work_contents;
        }

    }

    /**
     * 内部メソッド バイナリデータ受信時の解析処理を実施
     * 
     * @param data
     *            ユーザデータ全体
     */
    private void analyzeBinary(final byte[] data) {

        // -----------------------------------
        // 電文ヘッディング部解析
        // -----------------------------------
        byte[] heading = Arrays.copyOfRange(data, bch.getBchLength(),
                bch.getBchLength() + bch.getAnLength());
        log.finest("電文ヘッディングレングス [" + heading.length + "]");
        StringBuilder sb = new StringBuilder();
        sb.append("電文ヘッディング内容 [");
        for (int i = 0; i < heading.length; i++) {
            sb.append(String.format("%02x ", heading[i]));
        }
        sb.append("]");
        log.finest(sb.toString());

        // -----------------------------------
        // 本文部解析
        // -----------------------------------
        // 本文データ取得（(全データからBCHとA/N桁数分を引いた値)
        byte[] work_contents = Arrays.copyOfRange(data, bch.getBchLength()
                + bch.getAnLength(), data.length);
        log.finest("本文レングス [" + work_contents.length + "]");

        // gzip圧縮のファイルの場合は解凍処理
        if (this.bch.getXmlType() == BCH.XMLTYPE_XML_ON_GZIP) {
            this.contents = ZipHelper.ungzip(work_contents);
        }
        // zip圧縮のファイルの場合も解凍処理
        else if (this.bch.getXmlType() == BCH.XMLTYPE_XML_ON_ZIP) {
            this.contents = ZipHelper.unzip(work_contents);
        }
    }

    /**
     * 電文ヘディング部の解析<br>
     * 内容はパターン1、パターン3どちらも共通のため内部メソッドとして外出しにする
     * 
     * @param heading
     */
    private void analyzeHeading(final byte[] heading) {

        // 最初のデータはデータ種別コードまたは冒頭符
        this.headerCode = this.substring(heading, 0, NL, SP);
        log.finest("電文ヘッディング データ種別コード/冒頭符 -> [" + this.headerCode + "]");
        // 次のデータは発信官署名
        this.senderSign = this.substring(heading, this.headerCode.length(), SP,
                SP);
        log.finest("電文ヘッディング 発信官署名 -> [" + this.senderSign + "]");

        // 次のデータは観測日時刻
        this.observationDate = this.substring(heading, this.headerCode.length()
                + this.senderSign.length(), SP, SP);

        // 最後のデータは指定コード
        this.appointCode = this.substring(heading, this.headerCode.length()
                + this.senderSign.length() + this.observationDate.length(), SP,
                0);

    }

    /**
     * デリミタの指定がない場合は0を指定<br>
     * beginDelimtが0の場合はデータの先頭から、endDelimitが0の場合はデータの最後尾まで指定したことと同意とする。
     * 
     * @param data
     * @param beginDelimit
     * @param endDelimit
     * @return
     */
    private String substring(byte[] data, int beginIndex, int beginDelimit,
            int endDelimit) {
        int beginDelimitIndex = -1;
        int endDelimitIndex = -1;
        // 開始デリミタの位置取得
        int i = beginIndex;
        for (; i < data.length; i++) {
            byte abyte = data[i];
            if (abyte == beginDelimit) {
                beginDelimitIndex = i;
                break;
            }
        }
        // 終了デリミタの位置取得
        if (endDelimit == 0) {
            endDelimitIndex = data.length - 1;
        } else {
            for (; i < data.length; i++) {
                byte abyte = data[i];
                if (abyte == endDelimit) {
                    endDelimitIndex = i;
                    break;
                }
            }
        }
        // デリミタが見つからない場合はnull返却
        if (beginDelimitIndex == -1 || endDelimitIndex == -1) {
            return null;
        }

        String retStr = null;
        try {
            retStr = new String(Arrays.copyOfRange(data, beginDelimitIndex + 1,
                    endDelimitIndex), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return retStr;
    }

    /**
     * 本文を取得します。<br>
     * 存在しない場合はnullを返却します。
     * 
     * @return
     */
    @Override
    public byte[] getContents() {
        return this.contents;
    }

    /**
     * データ解析結果からデータの種類を返却します。<br>
     * データ種類は<code>Consts</code>クラスに定義しているものを使用する。<br>
     */
    @Override
    public String getDataType() {
        // JMA受信電文のデータタイプは 'XML' 'PDF' BUF' GRI' のいずれか
        // BCHヘッダの内容で上記のいずれかを判定する
        int majorDataType = bch.getMajorDataType();
        int minorDataType = bch.getMinorDataType();
        if (majorDataType == 6) {
            if (minorDataType == 3) {
                return Consts.QUEUE_DATA_TYPE_BUF;
            } else if (minorDataType == 4) {
                return Consts.QUEUE_DATA_TYPE_GRI;
            }
        }
        // TODO PDFの判定方法は？
        
        return Consts.QUEUE_DATA_TYPE_XML;
    }
}
