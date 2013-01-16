//
//  AppConfig.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs;

import java.util.HashMap;

/**
 * アプリケーションの設定ファイル内容を保持するクラス.<br>
 * 
 */
public class AppConfig {

    private HashMap<String, String> configMap;

    /**
     * 自身のシングルトンインスタンス
     */
    private static AppConfig instance;

    /**
     * シングルトン設計
     */
    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    /**
     * コンストラクタ
     */
    private AppConfig() {
        configMap = new HashMap<String, String>();
    }

    /**
     * 設定内容保存
     */
    public void putConfig(String key, String value) {
        configMap.put(key, value);
    }

    /**
     * 設定内容取得
     */
    public String getConfig(String key) {
        return configMap.get(key);
    }
}
