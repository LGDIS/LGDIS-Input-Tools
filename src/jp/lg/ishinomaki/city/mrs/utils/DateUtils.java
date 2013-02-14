package jp.lg.ishinomaki.city.mrs.utils;

import java.util.Date;

public class DateUtils {

    /**
     * 2つの日付の差を求めます。
     * java.util.Date 型の日付 date1 - date2 が何分かを返します。
     * 
     * 計算方法は以下となります。
     * 1.最初に2つの日付を long 値に変換します。
     * 　※この long 値は 1970 年 1 月 1 日 00:00:00 GMT からの
     * 　経過ミリ秒数となります。
     * 2.次にその差を求めます。
     * 3.上記の計算で出た数量を 1 日の時間で割ることで
     * 　日付の差を求めることができます。
     * 　※1 日 ( 24 時間) は、86,400,000 ミリ秒です。
     * 
     * @param date1
     *            日付 java.util.Date
     * @param date2
     *            日付 java.util.Date
     * @return 2つの日付の差
     */
    public static int differenceMinutes(Date date1, Date date2) {
        long datetime1 = date1.getTime();
        long datetime2 = date2.getTime();
        long one_minute = 1000 * 60;
        long diffMinutes = (datetime1 - datetime2) / one_minute;
        return (int) diffMinutes;
    }

}
