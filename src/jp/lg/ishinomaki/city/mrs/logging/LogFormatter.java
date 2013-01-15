//
//  LogFormatter.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * JMA受信サーバアプリで使用するログのフォーマットクラス
 * 
 */
public class LogFormatter extends Formatter {

    
    /**
     * 日付フォーマット
     */
    private final SimpleDateFormat sdFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * ログ出力時のフォーマット
     */
    public String format(final LogRecord logRecord) {

        StringBuilder sb = new StringBuilder();

        // 先頭は日付
        sb.append(sdFormat.format(new Date(logRecord.getMillis())));
        sb.append(" ");

        // ログレベル
        // FINEST,FINE,FINER,CONFIGの場合はデバッグ用のためクラス名とメソッド名を付与
        if (logRecord.getLevel() == Level.FINEST
                || logRecord.getLevel() == Level.FINE
                || logRecord.getLevel() == Level.FINER
                || logRecord.getLevel() == Level.CONFIG) {
            sb.append("[DEBUG]").append(logRecord.getSourceClassName())
                    .append(".").append(logRecord.getSourceMethodName())
                    .append(" - ");
        } else if (logRecord.getLevel() == Level.INFO) {
            sb.append("-");
        } else if (logRecord.getLevel() == Level.WARNING) {
            sb.append("*");
        } else if (logRecord.getLevel() == Level.SEVERE) {
            sb.append("!");
        } else {
            sb.append(" ");
        }
        sb.append(" ");

        // メッセージ
        sb.append(logRecord.getMessage());
        sb.append("\n");

        return sb.toString();
    }
}
