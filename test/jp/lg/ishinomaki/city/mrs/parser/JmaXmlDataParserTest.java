package jp.lg.ishinomaki.city.mrs.parser;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

public class JmaXmlDataParserTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        String actual = "abc";
        String expected = "def";
        assertThat(actual, is(expected));
    }
    
    @Test
    public void Control部取得成功() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Control><Title>季節観測</Title></Control></Report>";
        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser();
        
        InputStream bis = new ByteArrayInputStream(xml.getBytes());
        DocumentBuilder db;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(bis);
            XPath xpath = XPathFactory.newInstance().newXPath();
            JmaParseRule rule = JmaParseRule.getInstance();
            
            target.parseXmlControl(doc, xpath, rule);
            String actual = target.getXmlControl();
            assertThat(actual, is("<Control><Title>季節観測</Title></Control>"));
        } catch (Exception e) {
            fail();
        }
    }
    
    public void Control部なし() {
        
    }
    
    public void Control部取得失敗() {
        
    }

}
