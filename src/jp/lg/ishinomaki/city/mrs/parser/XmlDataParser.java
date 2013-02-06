//
//  XmlDataParser.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.parser;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML解析のための抽象クラスです.
 * 
 */
public abstract class XmlDataParser {

    /**
     * コンストラクタです。
     */
    public XmlDataParser() {
    }

    /**
     * XMLを解析します.<br>
     * 抽象メソッドです。継承先で実装してください。<br>
     * 
     * @param data
     *            解析対象のXML文字列
     * @return boolean true 解析成功 false 解析失敗
     */
    abstract public boolean parse(String xml);

    /**
     * Documentからxpathで指定したテキストを取得します.<br>
     * 
     * @param xpath
     * @param path
     * @param doc
     * @return String 取得テキスト xpath上にテキストがない場合はnullを返却
     */
    String stringByXpath(XPath xpath, String path, Object item) {
        String ret = null;
        try {
            ret = xpath.evaluate(path, item);
        } catch (Exception e) {
            // ログは不要
        }
        return ret;
    }

    /**
     * Documentからxpathで指定したNodeを取得します.<br>
     * 
     * @param xpath
     * @param path
     * @param doc
     * @return String 取得テキスト xpath上にテキストがない場合はnullを返却
     */
    Node nodeByXpath(XPath xpath, String path, Object item) {
        Node ret = null;
        try {
            ret = (Node) xpath.evaluate(path, item, XPathConstants.NODE);
        } catch (Exception e) {
            // ログは不要
        }
        return ret;
    }

    /**
     * Documentからxpathで指定したNodeのリストを取得します.<br>
     * 
     * @param xpath
     * @param path
     * @param doc
     * @return String 取得テキスト xpath上にテキストがない場合はnullを返却
     */
    NodeList nodelistByXpath(XPath xpath, String path, Object item) {
        NodeList ret = null;
        try {
            ret = (NodeList) xpath.evaluate(path, item, XPathConstants.NODESET);
        } catch (Exception e) {
            // ログは不要
        }
        return ret;
    }

}
