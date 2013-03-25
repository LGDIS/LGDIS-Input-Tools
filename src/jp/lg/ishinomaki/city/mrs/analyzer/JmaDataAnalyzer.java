//
//  JmaDataAnalyzer.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.analyzer;

import java.util.Arrays;
import java.util.logging.Logger;

import jp.lg.ishinomaki.city.mrs.Consts;
import jp.lg.ishinomaki.city.mrs.utils.ArchiveUtils;
import jp.lg.ishinomaki.city.mrs.utils.BCHChecksumHelper;

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
    BCH bch = null;

    /**
     * 電文ヘッディング部
     */
    byte[] heading = null;
    
    /**
     * 本文
     */
    byte[] contents = null;

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
            log.severe("チェックサムエラーのため処理を中断します。");
            log.severe("ただし処理は続行します"); // for test
            // TODO for test
            // チェックサムエラーでも後続処理を続行
            // throw new InvalidParameterException("チェックサムエラーのため処理を中断します。");
        } else {
            log.info("チェックサムOK");
        }

        // ヘッダ部の解析
        analyzeHeading(data);

        // 本文部の解析
        analyzeBody(data);
    }

    /**
     * ヘッディング部を解析する内部メソッド
     * 
     * @param data データ全体
     */
    void analyzeHeading(final byte[] data) {
        // -----------------------------------
        // 電文ヘッディング部解析
        // -----------------------------------
        byte[] work_heading = Arrays.copyOfRange(data, bch.getBchLength(),
                bch.getBchLength() + bch.getAnLength());
        log.finest("電文ヘッディングレングス [" + work_heading.length + "]");
        StringBuilder sb = new StringBuilder();
        sb.append("電文ヘッディング内容 [");
        for (int i = 0; i < work_heading.length; i++) {
            sb.append(String.format("%02x ", work_heading[i]));
        }
        sb.append("]");
        // 現時点ではログを出力するのみ
        log.finest(sb.toString());

        this.heading = work_heading;
    }

    /**
     * 本文部を解析する内部メソッド
     * 
     * @param data データ全体
     */
    void analyzeBody(final byte[] data) {
        // -----------------------------------
        // 本文部解析
        // -----------------------------------
        // 本文データ取得（(全データからBCHとA/N桁数分を引いた値)
        byte[] work_contents = Arrays.copyOfRange(data, bch.getBchLength()
                + bch.getAnLength(), data.length);
        log.finest("本文レングス [" + work_contents.length + "]");

        // gzip圧縮のファイルの場合は解凍処理
        if (this.bch.getXmlType() == BCH.XMLTYPE_XML_ON_GZIP) {
            this.contents = ArchiveUtils.ungzip(work_contents);
        }
        // zip圧縮のファイルの場合も解凍処理
        else if (this.bch.getXmlType() == BCH.XMLTYPE_XML_ON_ZIP) {
            this.contents = ArchiveUtils.unzip(work_contents);
        }
        // 圧縮なしのXML
        else if (this.bch.getXmlType() == BCH.XMLTYPE_XML) {
            this.contents = work_contents;
        }
        // 上記以外
        else {
            this.contents = work_contents;
        }
    }

    /**
     * 本文を取得します。<br>
     * 存在しない場合はnullを返却します。
     * 
     * @return byte[] 本文内容
     */
    @Override
    public byte[] getContents() {
        return this.contents;
    }

    /**
     * データ解析結果からデータの種類を返却します。<br>
     * データ種類は<code>Consts</code>クラスに定義しているものを使用する。<br>
     * 
     * @return String データ種別
     */
    @Override
    public String getDataType() {
        // JMA受信電文のデータタイプは 'XML' 'PDF' BUF' GRI' のいずれか
        // BCHヘッダの内容で上記のいずれかを判定する
        int majorDataType = bch.getMajorDataType();
        int minorDataType = bch.getMinorDataType();
        if (majorDataType == 6) {
            if (minorDataType == 3) {
                return Consts.DATA_TYPE_BUF;
            } else if (minorDataType == 4) {
                return Consts.DATA_TYPE_GRI;
            }
        }
        // TODO それ以外のファイル形式の判定方法は？

        return Consts.DATA_TYPE_XML;
    }
}
