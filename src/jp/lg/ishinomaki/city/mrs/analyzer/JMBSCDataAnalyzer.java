//
//  JMBSCDataAnalyzer.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.analyzer;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.logging.Logger;

public class JMBSCDataAnalyzer implements DataAnalyzer {

    /**
     * 当クラスのロガーインスタンス
     */
    private final Logger log = Logger.getLogger(JMBSCDataAnalyzer.class
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
     * コンストラクタ 特に処理はありません。
     */
    public JMBSCDataAnalyzer() {
        super();
    }

    @Override
    public void analyze(final byte[] data) {
        log.fine("start");

        // -----------------------------------
        // BCH作成
        // -----------------------------------
        this.bch = new BCH(data);
        log.info(this.bch.toString()); // デバッグ用ログ出力

        // -----------------------------------
        // 電文ヘッディング部解析
        // パターン1(テキスト本文)とパターン3(バイナリ本文)のみ考慮
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
            // パターン3
            // BCHからヘッディング長取得
            // 電文ヘディング部取得
            heading = Arrays.copyOfRange(data, bch.getBchLength(),
                    bch.getBchLength() + bch.getAnLength());
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
     * 電文ヘディング部の解析<br>
     * 内容はパターン1、パターン3どちらも共通のため内部メソッドとして外出しにする
     * 
     * @param heading
     */
    private void analyzeHeading(final byte[] heading) {

        // 最初のデータはデータ種別コードまたは冒頭符
        this.headerCode = this.substring(heading, 0, NL, SP);

        // 次のデータは発信官署名
        this.senderSign = this.substring(heading, this.headerCode.length(), SP,
                SP);

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
     * BCHヘッダー部を取得します。
     * 
     * @return BCH BCHインスタンス
     */
    @Override
    public BCH getBCH() {
        return this.bch;
    }

    /**
     * 冒頭符部分を取得します。<br>
     * 冒頭符が存在しない場合はnullを返却します。
     * 
     * @return String 冒頭符
     */
    @Override
    public String getHeaderCode() {
        return this.headerCode;
    }

    /**
     * 発信官署名を取得します。<br>
     * 発信官署名が存在しない場合はnullを返却します。
     * 
     * @return String 発信官署名
     */
    @Override
    public String getSenderSign() {
        return this.senderSign;
    }

    /**
     * 観測日時刻を取得します。<br>
     * 観測日時刻が存在しない場合はnullを返却します。
     * 
     * @return String 観測日時刻
     */
    @Override
    public String getObservationDate() {
        return this.observationDate;
    }

    /**
     * 指定コードを取得します。<br>
     * 指定コードが存在しない場合はnullを返却します。
     * 
     * @return String 指定コード
     */
    @Override
    public String getAppointCode() {
        return this.appointCode;
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

}
