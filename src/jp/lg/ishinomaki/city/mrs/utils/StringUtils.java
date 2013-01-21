//
//  StringUtils.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.utils;

/**
 * String関連のユーティリティメソッドを集めたクラスです。
 */
public class StringUtils {

    /**
     * 引数の文字列がnullまたは空文字または空白文字だけの場合はtrueを返却します。
     * 
     * @param s 
     * @return true:ブランク文字の場合 false:ブランク文字以外の場合
     */
    public static boolean isBlank(String s) {
        if (s == null) {
            return true;
        } else if (s.trim().length() == 0) {
            return true;
        }
        return false;
    }
}
