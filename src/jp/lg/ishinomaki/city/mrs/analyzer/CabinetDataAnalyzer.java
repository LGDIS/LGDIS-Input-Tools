//
//  CabinetDataAnalyzer.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.analyzer;

import jp.lg.ishinomaki.city.mrs.Consts;

/**
 * J-Alertの内閣データの解析を行う
 * 
 */
public class CabinetDataAnalyzer implements DataAnalyzer {

    /**
     * データの本文を保存する変数
     */
    private byte[] contents = null;

    @Override
    public void analyze(byte[] data) {
        // tar形式のデータをそのまま使用するため特に解析は行わない
        // [注意]ここではtarの解凍は行わない
        // 内容を解析する必要がないためparser側で処理を行う
        contents = data;
    }

    @Override
    public byte[] getContents() {
        return contents;
    }

    @Override
    public String getDataType() {
        // 内閣データはTarファイル固定
        return Consts.DATA_TYPE_TAR;
    }

}
