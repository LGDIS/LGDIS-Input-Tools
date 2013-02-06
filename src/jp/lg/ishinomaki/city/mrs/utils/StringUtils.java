//
//  StringUtils.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.utils;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

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

    /**
     * 震度の大小を比較する.<br>
     * 震度には"3","4","5-","5+","6-","6+","7"の文字列が指定される<br>
     * 第一引数の方が大きい場合はtrue,第二引数の方が大きい場合はfalseを返却<br>
     * 第一引数と第二引数が同値の場合はtrueを返却
     * 
     * @param str1
     * @param str2
     * @return
     */
    public static boolean compareSeismicIntensity(String str1, String str2) {

        double d1 = convertSeismicIntensityToDouble(str1);
        double d2 = convertSeismicIntensityToDouble(str2);
        if (d1 >= d2) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 震度を表す文字列をdouble型数値に変換.<br>
     * 5-は4.5、5+は5.5といった変換を行う
     * 
     * @param str
     * @return
     */
    public static double convertSeismicIntensityToDouble(String str) {
        double d = 0;
        // 1桁の文字はそのままdouble型に変換
        // 2桁の文字は2桁目が+の場合は+0.5、2桁目が-の場合は-0.5してdouble型に変換
        if (str.length() == 1) {
            d = Float.parseFloat(str);
        } else if (str.length() == 2) {
            if (str.substring(1, 2).equals("+")) {
                d = Float.parseFloat(str.substring(0, 1)) + 0.5;
            } else if (str.substring(1, 2).equals("-")) {
                d = Float.parseFloat(str.substring(0, 1)) - 0.5;
            }
        }
        return d;
    }

    /**
     * point情報をRest用文字列に変換する内部メソッド.<br>
     * +21.2+135.5/ -> (135.5,21.2)<br>
     * 深さの値は捨てる<br>
     * 
     * @return
     */
    public static String convertPoint(String val) {
        if (StringUtils.isBlank(val)) {
            return null;
        }
        // 正規表現を利用
        Pattern p = Pattern.compile("([¥+¥-][0-9.]+)");
        Matcher m = p.matcher(val);
        String lat = null;
        String lng = null;
        // 1つ目の要素は緯度
        if (m.find()) {
            lat = m.group();
        } else {
            return null;
        }
        // 2つ目の要素は経度
        if (m.find()) {
            lng = m.group();
        } else {
            return null;
        }
        // !3つ目に深さの要素が存在する場合があるが深さは使用しないため無視する

        // (+136,+35)形式の文字列を作成
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(lng).append(",").append(lat).append(")");
        return sb.toString();
    }

    /**
     * line情報をRest用文字列に変換する内部メソッド.<br>
     * +35+138/+36+138/+37+138/+38+138/ -> [(138,35),(36,138),(37,138),(38,138)]<br>
     * 深さの値は捨てる<br>
     * 
     * @return
     */
    public static String convertLine(String val) {
        return convertPointArray(val, "[", "]");
    }

    /**
     * point情報をRest用文字列に変換する内部メソッド.<br>
     * +35+138/+36+138/+37+138/+38+138/ -> ((138,35),(138,36),(138,37),(138,38))<br>
     * 深さの値は捨てる<br>
     * 
     * @return
     */
    public static String convertPolygon(String val) {
        return convertPointArray(val, "(", ")");
    }

    /**
     * 
     * @param val
     *            +35+135/+36+136/
     * @param beginMark
     *            "["や"("など
     * @param endMark
     *            "]"や")"など
     * @return ((+135,+35),(+136,+36))
     */
    public static String convertPointArray(String val, String beginMark,
            String endMark) {
        if (StringUtils.isBlank(val) || StringUtils.isBlank(beginMark)
                || StringUtils.isBlank(endMark)) {
            return null;
        }

        // この変数内に地理系情報(文字列)を格納する
        ArrayList<String> points = new ArrayList<String>();

        // 位置情報を"/"で区切る
        StringTokenizer slashTokenizer = new StringTokenizer(val, "/");
        while (slashTokenizer.hasMoreTokens()) {
            String point = slashTokenizer.nextToken();
            String convertedPoint = convertPoint(point);
            points.add(convertedPoint);
        }

        if (points.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(beginMark);
            for (int i = 0; i < points.size(); i++) {
                sb.append(points.get(i));
                if (i != (points.size() - 1)) {
                    sb.append(",");
                }
            }
            sb.append(endMark);

            return sb.toString();
        }
        return null;
    }

    /**
     * ノードの要素値を文字列に変換します。 ノード内にタグが含まれている場合、そのタグは自動整形されます。
     * 
     * @param node
     *            変換対象の文字列
     * @return 変換後文字列
     * @throws TransformerException
     *             XML規約に違反している場合に発生する例外
     */
    public static String convertToString(Node node) throws TransformerException {
        // ノードをXMLとして定義します
        DOMSource source = new DOMSource(node);
        // 文字列生成用ストリーム
        StringWriter swriter = new StringWriter();
        StreamResult result = new StreamResult(swriter);
        // XMLを文字列に変換します
        transform(source, result);
        return swriter.toString();
    }

    /**
     * XML変換エンジン呼び出しです。
     * 
     * @param source
     *            変換対象のXML
     * @param result
     *            変換後文字列
     * @throws TransformerException
     *             XML規約に違反している場合に発生する例外
     */
    private static void transform(Source source, Result result)
            throws TransformerException {
        // 変換エンジンを取得します
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();
        // XML変換のルールを設定します
        // XML宣言
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        // 文字コード
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        
        // 変換
        transformer.transform(source, result);
    }
}
