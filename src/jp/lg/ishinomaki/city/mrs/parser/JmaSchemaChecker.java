package jp.lg.ishinomaki.city.mrs.parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * JMAから受信したXMLファイルのスキーマチェックを行うクラスです。
 * 
 */
public class JmaSchemaChecker {

    // ログ用
    private static Logger log = Logger.getLogger(JmaSchemaChecker.class
            .getSimpleName());

    /**
     * シングルトン設計.<br>
     * 自インスタンス変数
     */
    private static JmaSchemaChecker instance;

    /**
     * XLMスキーマチェック用インスタンス
     */
    public static SchemaFactory factory;
    public static Schema schema;
    public static Validator validator;

    /**
     * インスタンス取得
     * 
     * @return
     */
    public static JmaSchemaChecker getInstatnce() {
        if (instance == null) {
            instance = new JmaSchemaChecker();
        }
        return instance;
    }

    /**
     * シングルトン設計のためプライベートなコンストラクタ
     */
    private JmaSchemaChecker() {

        // 1. Lookup a factory for the W3C XML Schema language
        factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        String schemaFilePath = ParserConfig.getInstance().getSchemaFilePath();

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
