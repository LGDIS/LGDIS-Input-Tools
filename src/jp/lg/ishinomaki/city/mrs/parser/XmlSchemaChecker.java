//
//  SchemaChecker.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import jp.lg.ishinomaki.city.mrs.utils.StringUtils;

import org.xml.sax.SAXException;

/**
 * XMLファイルのスキーマチェックを行うクラスです。
 * 
 */
public class XmlSchemaChecker {

    // ログ用
    private static Logger log = Logger.getLogger(XmlSchemaChecker.class
            .getSimpleName());

    /**
     * シングルトン設計.<br>
     * 自インスタンス変数
     */
    private static XmlSchemaChecker instance;

    /**
     * XLMスキーマチェック用インスタンス
     */
    public static SchemaFactory factory;
    public static Schema schema;
    public static Validator validator;

    /**
     * XMLスキーマチェック不要フラグ. XMLスキーマファイル名が定義ファイルに定義されていない場合はチェックを行いません。
     */
    boolean disused = false;

    /**
     * インスタンス取得
     * 
     * @return
     */
    public static XmlSchemaChecker getInstatnce(String schemaFilePath) {
        if (instance == null) {
            instance = new XmlSchemaChecker(schemaFilePath);
        }
        return instance;
    }

    /**
     * シングルトン設計のためプライベートなコンストラクタ.
     * 引数にスキーマファイルを指定する
     */
    private XmlSchemaChecker(String schemaFilePath) {

        // スキーマファイル名が定義ファイルに定義されていない場合はチェック不要フラグON
        if (StringUtils.isBlank(schemaFilePath)) {
            disused = true;
            return;
        }

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

        // スキーマチェック不要フラグONの場合はチェック不要
        if (disused) {
            return true;
        }

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
