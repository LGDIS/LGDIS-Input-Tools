//
//  XmlSchemaChecker.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * XMLファイルのスキーマチェックを行うクラスです。
 * 
 */
public class XmlSchemaChecker {

    // ログ用
    private static Logger log = Logger.getLogger(XmlSchemaChecker.class
            .getSimpleName());

    private static Map<String, XmlSchemaChecker> instanceMap;
    static {
        instanceMap = new HashMap<String, XmlSchemaChecker>();
    }

    /**
     * XLMスキーマチェック用インスタンス
     */
    private SchemaFactory factory;
    private Schema schema;
    private Validator validator;

    /**
     * インスタンス取得
     * 
     * @return
     */
    public static XmlSchemaChecker getInstatnce(String schemaFilePath) {

        XmlSchemaChecker instance = instanceMap.get(schemaFilePath);
        if (instance == null) {
            instance = new XmlSchemaChecker(schemaFilePath);
        }
        instanceMap.put(schemaFilePath, instance);

        return instance;
    }

    /**
     * シングルトン設計のためプライベートなコンストラクタ. 引数にスキーマファイルを指定する
     */
    private XmlSchemaChecker(String schemaFilePath) {

        // 1. Lookup a factory for the W3C XML Schema language
        factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        // 2. Compile the schema.
        File schemaLocation = new File(schemaFilePath);
        try {
            schema = factory.newSchema(schemaLocation);
        } catch (SAXException e) {
            e.printStackTrace();
            return;
        }

        // 3. Get a validator from the schema.
        validator = schema.newValidator();

    }

    /**
     * スキーマチェック実施
     * 
     * @param xml
     * @return
     */
    public boolean validate(String xml) {

        // 4. Parse the document you want to check.
        Source source = new StreamSource(new ByteArrayInputStream(
                xml.getBytes()));

        // 5. Check the document
        try {
            validator.validate(source);
            // 例外が発生しなかったらスキーマチェックOK
            return true;
        } catch (Exception e) {
            log.severe("スキーマチェックNG : " + e.getMessage());
            return false;
        }
    }
}
