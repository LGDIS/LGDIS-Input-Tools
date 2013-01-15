package jp.lg.ishinomaki.city.mrs.utils;

import org.w3c.dom.Document;

public class XmlSchemeValidator {
    
    
    public static boolean validate(Document doc, String schemeFile) {
        
        /*
        // スキーマの生成
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new File("schema/jmx.xsd"));
        // バリデータの生成
        Validator validator = schema.newValidator();
        // エラーハンドラーの設定
        validator.setErrorHandler(errorHandler)
        // 妥当性検証
        validator.validate(source)
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        SchemaFactory sFac;
        Schema schema;
    
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setSchema(schema);
        builder = factory.newDocumentBuilder();
    
        Document doc2 = builder.parse(new File("./sample.xml"));
    
        // 妥当性検証
        schema = sFac.newSchema(new File("./sample.xsd"));
        Validator validator = schema.newValidator();
        validator.validate(new DOMSource(doc));
        return false;
        */
        return true;
    }
    
}
