package jp.lg.ishinomaki.city.mrs.parser;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

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

public class KsnXmlDataParserTest {

    static XPath xpath;
    static KsnParseRule rule;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ParserConfig config = ParserConfig.getInstance();
        config.loadYml("test/config/parser_KsnXmlDataParserTest.yml");   // テスト用のconfigファイル置き場
        xpath = XPathFactory.newInstance().newXPath();
        rule = KsnParseRule.getInstance();
    }

    // テスト用メソッド
    public Document loadDocument(String xml) {
        // テスト対象のXML文字列を読み込みDocumentを取得
        InputStream bis = new ByteArrayInputStream(xml.getBytes());
        DocumentBuilder db;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(bis);
            return doc;
        } catch (Exception e) {
        }
        return null;
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
    public void Head部取得成功() {
        // テスト用XML読み込み
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WarningReport cause=\"0\" apply=\"1\" xsi:schemaLocation=\"http://wrtrpt.unify.river.go.jp wrn_sample.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://wrtrpt.unify.river.go.jp\"><!--水防警報サンプル--><Summary><JursdctnOffice><RvrSys rvrSysCode=\"85053000\"><Name>庄内川水系</Name></RvrSys></JursdctnOffice></Summary></WarningReport>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        KsnXmlDataParser target = new KsnXmlDataParser();

        target.parseXmlHead(doc, xpath, rule);
        String actual = target.getXmlHead();

        assertThat(
                actual,
                is("<Summary><JursdctnOffice><RvrSys rvrSysCode=\"85053000\"><Name>庄内川水系</Name></RvrSys></JursdctnOffice></Summary>"));

    }

    @Test
    public void Head部なし() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WarningReport cause=\"0\" apply=\"1\" xsi:schemaLocation=\"http://wrtrpt.unify.river.go.jp wrn_sample.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://wrtrpt.unify.river.go.jp\"><!--水防警報サンプル--><Sammary><JursdctnOffice><RvrSys rvrSysCode=\"85053000\"><Name>庄内川水系</Name></RvrSys></JursdctnOffice></Sammary></WarningReport>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        KsnXmlDataParser target = new KsnXmlDataParser();

        target.parseXmlHead(doc, xpath, rule);
        String actual = target.getXmlHead();

        assertThat(actual, is(nullValue()));
    }

    @Test
    public void Body部取得成功() {
        // テスト用XML読み込み
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WarningReport cause=\"0\" apply=\"1\" xsi:schemaLocation=\"http://wrtrpt.unify.river.go.jp wrn_sample.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://wrtrpt.unify.river.go.jp\"><!--水防警報サンプル--><Detail><Text xml:space=\"preserve\">12時40分現在土岐の水位は2m70cmで引き続きかんまんに減水中である。本地区の水防警報を解除する。降雨もなく、洪水のおそれがなくなったので</Text></Detail></WarningReport>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        KsnXmlDataParser target = new KsnXmlDataParser();

        target.parseXmlBody(doc, xpath, rule);
        String actual = target.getXmlBody();

        assertThat(
                actual,
                is("<Text xml:space=\"preserve\">12時40分現在土岐の水位は2m70cmで引き続きかんまんに減水中である。本地区の水防警報を解除する。降雨もなく、洪水のおそれがなくなったので</Text>"));

    }

    @Test
    public void Body部なし() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WarningReport cause=\"0\" apply=\"1\" xsi:schemaLocation=\"http://wrtrpt.unify.river.go.jp wrn_sample.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://wrtrpt.unify.river.go.jp\"><!--水防警報サンプル--><Detail><Tax xml:space=\"preserve\">12時40分現在土岐の水位は2m70cmで引き続きかんまんに減水中である。本地区の水防警報を解除する。降雨もなく、洪水のおそれがなくなったので</Tax></Detail></WarningReport>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        KsnXmlDataParser target = new KsnXmlDataParser();

        target.parseXmlBody(doc, xpath, rule);
        String actual = target.getXmlBody();

        assertThat(actual, is(nullValue()));
    }

    @Test
    public void プロジェクトID取得() {

        // テストターゲットクラス
        KsnXmlDataParser target = new KsnXmlDataParser();

        // cause=0のプロジェクトID
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WarningReport cause=\"0\" apply=\"1\" xsi:schemaLocation=\"http://wrtrpt.unify.river.go.jp wrn_sample.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://wrtrpt.unify.river.go.jp\"></WarningReport>";
        Document doc = loadDocument(xml);
        target.parseProjectId(doc, xpath, rule);
        String actual = target.getProjectId();
        assertThat(actual, is("I04202000000000000001"));

        // cause=1のプロジェクトID
        String xml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WarningReport cause=\"1\" apply=\"1\" xsi:schemaLocation=\"http://wrtrpt.unify.river.go.jp wrn_sample.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://wrtrpt.unify.river.go.jp\"></WarningReport>";
        Document doc2 = loadDocument(xml2);
        target.parseProjectId(doc2, xpath, rule);
        String actual2 = target.getProjectId();
        assertThat(actual2, is("I04202000000000000001"));

        // cause=2のプロジェクトID
        String xml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WarningReport cause=\"2\" apply=\"1\" xsi:schemaLocation=\"http://wrtrpt.unify.river.go.jp wrn_sample.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://wrtrpt.unify.river.go.jp\"></WarningReport>";
        Document doc3 = loadDocument(xml3);
        target.parseProjectId(doc3, xpath, rule);
        String actual3 = target.getProjectId();
        assertThat(actual3, is("training-project"));

        // cause=3のプロジェクトID
        String xml4 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WarningReport cause=\"3\" apply=\"1\" xsi:schemaLocation=\"http://wrtrpt.unify.river.go.jp wrn_sample.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://wrtrpt.unify.river.go.jp\"></WarningReport>";
        Document doc4 = loadDocument(xml4);
        target.parseProjectId(doc4, xpath, rule);
        String actual4 = target.getProjectId();
        assertThat(actual4, is("I04202000000000000001"));

        // causeなしのプロジェクトID
        String xml5 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WarningReport apply=\"1\" xsi:schemaLocation=\"http://wrtrpt.unify.river.go.jp wrn_sample.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://wrtrpt.unify.river.go.jp\"></WarningReport>";
        Document doc5 = loadDocument(xml5);
        target.parseProjectId(doc5, xpath, rule);
        String actual5 = target.getProjectId();
        assertThat(actual5, is("I04202000000000000001"));

        // cause=10のプロジェクトID
        String xml6 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WarningReport cause=\"10\" apply=\"1\" xsi:schemaLocation=\"http://wrtrpt.unify.river.go.jp wrn_sample.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://wrtrpt.unify.river.go.jp\"></WarningReport>";
        Document doc6 = loadDocument(xml6);
        target.parseProjectId(doc6, xpath, rule);
        String actual6 = target.getProjectId();
        assertThat(actual6, is("I04202000000000000001"));
    }

    @Test
    public void Issue拡張フィールド取得() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WarningReport cause=\"0\" apply=\"1\" xsi:schemaLocation=\"http://wrtrpt.unify.river.go.jp wrn_sample.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://wrtrpt.unify.river.go.jp\"><!--水防警報サンプル--><Summary><JursdctnOffice><RvrSys rvrSysCode=\"85053000\"><Name>庄内川水系</Name><River riverCode=\"85053001\"><Name>庄内川</Name></River><ObsrvtnPoint obsrvtnPointCode=\"2\"><Name>土岐</Name></ObsrvtnPoint></RvrSys></JursdctnOffice><ReportKind reportKindCode=\"11\"><Name>水防警報</Name></ReportKind><ReportNo reportNoCode=\"15\">第15号</ReportNo><Time time=\"2003-11-24T12:40:00\"/><AnnOffice annOfficeCode=\"21782\" annOfficeSerialNo=\"1\"><Name>庄内川河川事務所</Name></AnnOffice><Alarm alarmCode=\"40\"><Name>解除</Name></Alarm><Text xml:space=\"preserve\">水防警報第15号平成15年11月24日12時40分庄内川河川事務所発表庄内川解除発表</Text></Summary><Detail><Text xml:space=\"preserve\">12時40分現在土岐の水位は2m70cmで引き続きかんまんに減水中である。本地区の水防警報を解除する。降雨もなく、洪水のおそれがなくなったので</Text></Detail></WarningReport>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        KsnXmlDataParser target = new KsnXmlDataParser();

        target.parseIssueExtraMap(doc, xpath, rule);
        Map<String, String> issueExtraMap = target.getIssueExtraMap();
        String actual = issueExtraMap.get("xml_control_cause");
        assertThat(actual, is("0"));
        actual = issueExtraMap.get("xml_control_apply");
        assertThat(actual, is("1"));
        actual = issueExtraMap.get("subject");
        assertThat(actual, is("水防警報"));
    }

}
