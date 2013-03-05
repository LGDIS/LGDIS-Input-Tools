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

public class JmaXmlDataParserTest {

    static XPath xpath;
    static JmaParseRule rule;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ParserConfig config = ParserConfig.getInstance();
        config.loadYml("test/config/parser_JmaXmlDataParserTest.yml");   // テスト用のconfigファイル置き場
        xpath = XPathFactory.newInstance().newXPath();
        rule = JmaParseRule.getInstance();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
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

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void Control部取得成功() {
        // テスト用XML読み込み
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Control><Title>季節観測</Title></Control></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseXmlControl(doc, xpath, rule);
        String actual = target.getXmlControl();

        assertThat(actual, is("<Control><Title>季節観測</Title></Control>"));

    }

    @Test
    public void Control部なし() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseXmlControl(doc, xpath, rule);
        String actual = target.getXmlControl();

        assertThat(actual, is(nullValue()));
    }

    @Test
    public void Head部取得成功() {
        // テスト用XML読み込み
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Head><Title>季節観測</Title></Head></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseXmlHead(doc, xpath, rule);
        String actual = target.getXmlHead();

        assertThat(actual, is("<Head><Title>季節観測</Title></Head>"));

    }

    @Test
    public void Head部なし() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseXmlHead(doc, xpath, rule);
        String actual = target.getXmlHead();

        assertThat(actual, is(nullValue()));
    }

    @Test
    public void Body部取得成功() {
        // テスト用XML読み込み
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Body><Title>季節観測</Title></Body></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseXmlBody(doc, xpath, rule);
        String actual = target.getXmlBody();

        assertThat(actual, is("<Body><Title>季節観測</Title></Body>"));

    }

    @Test
    public void Body部なし() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseXmlBody(doc, xpath, rule);
        String actual = target.getXmlBody();

        assertThat(actual, is(nullValue()));
    }

    @Test
    public void トラッカーID取得() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Control><Title>季節観測</Title></Control></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseTrackerId(doc, xpath, rule);
        String actual = target.getTrackerId();
        assertThat(actual, is("4"));

        String xml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Control><Title>指定河川洪水予報</Title></Control></Report>";
        Document doc2 = loadDocument(xml2);
        target.parseTrackerId(doc2, xpath, rule);
        String actual2 = target.getTrackerId();
        assertThat(actual2, is("3"));

        // デフォルトID取得
        String xml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Control><Title>あいうえお</Title></Control></Report>";
        Document doc3 = loadDocument(xml3);
        target.parseTrackerId(doc3, xpath, rule);
        String actual3 = target.getTrackerId();
        assertThat(actual3, is("4"));
    }

    @Test
    public void プロジェクトID取得() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Control><Status>通常</Status></Control></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseProjectId(doc, xpath, rule);
        String actual = target.getProjectId();
        assertThat(actual, is("I04202000000000000001"));

        String xml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Control><Status>訓練</Status></Control></Report>";
        Document doc2 = loadDocument(xml2);
        target.parseProjectId(doc2, xpath, rule);
        String actual2 = target.getProjectId();
        assertThat(actual2, is("training-project"));

        // デフォルトID取得
        String xml3 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Control><Status>あいうえお</Status></Control></Report>";
        Document doc3 = loadDocument(xml3);
        target.parseProjectId(doc3, xpath, rule);
        String actual3 = target.getProjectId();
        assertThat(actual3, is("I04202000000000000001"));
    }

    @Test
    public void Issue拡張フィールド取得() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Control><Status>通常</Status></Control></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseIssueExtraMap(doc, xpath, rule);
        Map<String, String> issueExtraMap = target.getIssueExtraMap();
        String actual = issueExtraMap.get("xml_control_status");

        assertThat(actual, is("通常"));
    }

    @Test
    public void 震度によるプロジェクト自動立ち上げあり() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Body><Intensity><Observation><Pref><Area><City><Name>石巻市</Name><MaxInt>6+</MaxInt></City></Area></Pref></Observation></Intensity></Body></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseIsAutoLaunchBySeismicIntensity(doc, xpath, rule);
        boolean actual = target.isAutoLaunch();

        assertThat(actual, is(true));
    }

    @Test
    public void 震度によるプロジェクト自動立ち上げなし() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Body><Intensity><Observation><Pref><Area><City><Name>石巻市</Name><MaxInt>4</MaxInt></City></Area></Pref></Observation></Intensity></Body></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseIsAutoLaunchBySeismicIntensity(doc, xpath, rule);
        boolean actual = target.isAutoLaunch();

        assertThat(actual, is(false));
    }

    @Test
    public void 震度によるプロジェクト自動立ち上げあり境界値震度５弱() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Body><Intensity><Observation><Pref><Area><City><Name>石巻市</Name><MaxInt>5-</MaxInt></City></Area></Pref></Observation></Intensity></Body></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseIsAutoLaunchBySeismicIntensity(doc, xpath, rule);
        boolean actual = target.isAutoLaunch();

        assertThat(actual, is(false));
    }

    @Test
    public void 震度によるプロジェクト自動送信あり() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Body><Intensity><Observation><Pref><Area><City><Name>石巻市</Name><MaxInt>4</MaxInt></City></Area></Pref></Observation></Intensity></Body></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseAutoSend(doc, xpath, rule);
        String actual = target.getDisposition();

        assertThat(actual, is("0"));
    }

    @Test
    public void 震度によるプロジェクト自動送信なし() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Body><Intensity><Observation><Pref><Area><City><Name>石巻市</Name><MaxInt>3</MaxInt></City></Area></Pref></Observation></Intensity></Body></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseAutoSend(doc, xpath, rule);
        String actual = target.getDisposition();

        assertThat(actual, is(nullValue()));
    }

    @Test
    public void 津波高さによるプロジェクト自動立ち上げあり() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Body><Tsunami><Estimation><Item><Area><Name>宮城金華山沖</Name></Area><MaxHeight><TsunamiHeight>3.3</TsunamiHeight></MaxHeight></Item></Estimation></Tsunami></Body></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseIsAutoLaunchByTsunamiHeight(doc, xpath, rule);
        boolean actual = target.isAutoLaunch();

        assertThat(actual, is(true));
    }

    @Test
    public void 津波高さによるプロジェクト自動立ち上げなし() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Body><Tsunami><Estimation><Item><Area><Name>宮城金華山沖</Name></Area><MaxHeight><TsunamiHeight>1</TsunamiHeight></MaxHeight></Item></Estimation></Tsunami></Body></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseIsAutoLaunchByTsunamiHeight(doc, xpath, rule);
        boolean actual = target.isAutoLaunch();

        assertThat(actual, is(false));
    }

    @Test
    public void 津波高さによるプロジェクト自動配信あり() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Body><Tsunami><Forecast><Item><Area><Name>宮城県</Name></Area><Category><Kind><Name>大津波警報：発表</Name></Kind></Category></Item></Forecast></Tsunami></Body></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseAutoSend(doc, xpath, rule);
        String actual = target.getDisposition();

        assertThat(actual, is("2"));
    }

    @Test
    public void 津波高さによるプロジェクト自動配信なし() {
        // テスト用XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Body><Forecast><Item><Area><Name>宮城県</Name></Area><Category><Kind><Name>大津波警報：発表テスト</Name></Kind></Category></Item></Forecast></Body></Report>";
        Document doc = loadDocument(xml);

        // テストターゲットクラス
        JmaXmlDataParser target = new JmaXmlDataParser("JMA");

        target.parseAutoSend(doc, xpath, rule);
        String actual = target.getDisposition();

        assertThat(actual, is(nullValue()));
    }
}
