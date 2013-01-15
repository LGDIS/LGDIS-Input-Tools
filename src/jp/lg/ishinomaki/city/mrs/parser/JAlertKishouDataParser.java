//
//  JAlertKishouDataParser.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * J-Alertから送信されたXMLデータを解析します。
 *
 */
public class JAlertKishouDataParser {

    /**
     * 当クラスのロガーインスタンス
     */
    private final Logger log = Logger.getLogger(JAlertKishouDataParser.class.getSimpleName());

    /**
     * Control部の情報を保持するテーブル 各種データの取り出しはこのクラスで定義する識別子を使用してください。
     */
    private Map<String, String> controlMap;

    /**
     * Head部の情報を保持するテーブル 各種データの取り出しはこのクラスで定義する識別子を使用してください。
     */
    private Map<String, String> headMap;

    /**
     * HeadlineのInformation内容を保持するテーブル
     * キーにInformationのtype要素、値にInformation要素のXMLをStringにしたものを保持します。
     */
    private Map<String, String> informationMap;

    /**
     * XMLのBody部分
     */
    private String body;
    
    
    /**
     * コンストラクタです。
     */
    public JAlertKishouDataParser() {
        this.controlMap = new HashMap<String, String>();
        this.headMap = new HashMap<String, String>();
        this.informationMap = new HashMap<String, String>();
    }

    /**
     * XMLを解析します。
     * 
     * @param data
     * @return
     */
    public boolean perse(String xml) {

        // TODO 以下は全てテストロジック xmlのパースは定義に基づき行う
        
        log.finest("start");

        try {
            Document doc = DocumentHelper.parseText(xml);

            // XPath:"/Report/Control/*"を指定するとControl配下が取れるはずだがReportの属性にnamespace:xmlnsが存在するとなぜかうまくいかない
            // そのため要素を1つづつ取り出すようにする

            Element controlElement = null;
            Element headElement = null;
            Element bodyElement = null;

            // Root要素(Report)取得
            Element rootElement = doc.getRootElement();
            // Root配下の3要素(Control,Head,Body)取得
            @SuppressWarnings("unchecked")
            List<Element> elements = rootElement.elements();
            for (Element anElement : elements) {
                if (anElement.getName().equals("Control")) {
                    controlElement = anElement;
                } else if (anElement.getName().equals("Head")) {
                    headElement = anElement;
                } else if (anElement.getName().equals("Body")) {
                    bodyElement = anElement;
                }
            }

            // ---------------------------------------
            // Control部抽出
            // ---------------------------------------
            // for debug log.finest("Control部");
            for (final Object anElement : controlElement.elements()) {
                Element element = (Element) anElement;
                String elementName = element.getName();
                String elementText = element.getText();
                controlMap.put(elementName, elementText);

                // for debug log.finest("要素名 -> " + elementName + ", 要素内容 -> " + elementText);
            }

            // ---------------------------------------
            // Head部抽出
            // ---------------------------------------
            // for debug log.finest("Head部");
            for (final Object anElement : headElement.elements()) {
                final Element element = (Element) anElement;

                String elementName = element.getName();
                String elementText = element.getText();
                headMap.put(elementName, elementText);

                // for debug log.finest("要素名 -> " + elementName + ", 要素内容 -> " + elementText);

                // Headlineの場合はさらに下位要素にTextとInformationが存在する
                if (elementName.equals("Headline")) {
                    // Headlineの配下要素を1つづつ走査
                    for (final Object anHeadlineElement : element.elements()) {
                        final Element headlineElement = (Element) anHeadlineElement;
                        String headlineElementName = headlineElement.getName();
                        // Text要素の場合は要素内Textを保存
                        if (headlineElementName.equals("Text")) {
                            // for debug log.finest("要素名 -> " + "Text" + ", 要素内容 -> " + headlineElement.getText());
                            headMap.put("Text", headlineElement.getText());
                        } else if (headlineElementName.equals("Information")) {
                            // Informationの場合はXMLをStringにしたものをリストに保存
                            String type = headlineElement
                                    .attributeValue("type");
                            String informationStr = headlineElement.asXML();
                            // for debug log.finest("要素名 -> " + type + ", 要素内容 -> " + informationStr);
                            informationMap.put(type, informationStr);
                        }
                    }
                }
            }

            // ---------------------------------------
            // Body部抽出
            // Body要素は存在しない場合もある
            // ---------------------------------------
            if (bodyElement != null) {
                this.body = bodyElement.asXML();
            }
            
        } catch (DocumentException e) {
            e.printStackTrace();
            log.severe("XMLの解析に失敗しました。");
            return false;
        }

        return true;
    }

    public Map<String, String> getControlMap() {
        return this.controlMap;
    }

    public Map<String, String> getHeadMap() {
        return this.headMap;
    }

    public Map<String, String> getInformationMap() {
        return this.informationMap;
    }
    
    public String getBody() {
        return this.body;
    }

}
