//
//  JAlertShoubouDataAnalyzer.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.analyzer;

import java.util.logging.Logger;

/**
 * J-Alertから受信する消防データの解析クラスです。
 * 
 */
public class JAlertShoubouDataAnalyzer implements DataAnalyzer {

    /**
     * 当クラスのロガーインスタンス
     */
    private final Logger log = Logger.getLogger(JAlertShoubouDataAnalyzer.class.getSimpleName());

    /**
     * コンストラクタ
     * 特に処理はありません。
     */
    public JAlertShoubouDataAnalyzer() {
        super();
    }
    
    @Override
    public void analyze(byte[] data) {
        log.finest("start");
    }

    @Override
    public BCH getBCH() {
        return null;
    }

    @Override
    public String getHeaderCode() {
        return null;
    }

    @Override
    public String getSenderSign() {
        return null;
    }

    @Override
    public String getObservationDate() {
        return null;
    }

    @Override
    public String getAppointCode() {
        return null;
    }

    @Override
    public byte[] getContents() {
        return null;
    }

}
