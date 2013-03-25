//
//  PngDataHandler.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.pickup;

import java.util.Map;

import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;

/**
 * PNG形式のデータをハンドリングするクラス
 * 
 */
public class PngDataHandler extends UploadDataHandler {

    /**
     * コンストラクタ
     * 
     * @param mode
     * @param inputId
     */
    public PngDataHandler(int mode, String inputId) {
        super(mode, inputId);
    }

    /**
     * Redmineへの設定情報を取得
     */
    @Override
    Map<String, String> getConfig() {
        return ParserConfig.getInstance().getPngAttachmentStatics();
    }

}
