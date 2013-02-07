package jp.lg.ishinomaki.city.mrs.parser;

import static org.junit.Assert.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

public class KsnXmlDataParserTest {

    static XPath xpath;
    static Document doc;
    static KsnParseRule rule;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ParserConfig config = ParserConfig.getInstance();
        config.loadYml("test/config/parser_KsnXmlDataParserTest.yml");   // テスト用のconfigファイル置き場
        xpath = XPathFactory.newInstance().newXPath();
        rule = KsnParseRule.getInstance();
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
        fail("Not yet implemented");
    }

}
