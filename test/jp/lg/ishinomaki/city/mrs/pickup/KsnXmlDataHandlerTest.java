package jp.lg.ishinomaki.city.mrs.pickup;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jp.lg.ishinomaki.city.mrs.parser.JmaXmlDataParser;
import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class KsnXmlDataHandlerTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ParserConfig config = ParserConfig.getInstance();
        config.loadYml("test/config/parser_KsnXmlDataHandlerTest.yml");
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
    public void 通常モード指定() throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException {
        // モード=0を指定
        JmaXmlDataHandler target = new JmaXmlDataHandler(0);

        // Mockオブジェクトに値を設定
        JmaXmlDataParser mock = Mockito.mock(JmaXmlDataParser.class);
        when(mock.getProjectId()).thenReturn("10000");
        when(mock.getTrackerId()).thenReturn("20000");
        when(mock.getXmlBody()).thenReturn("body");
        when(mock.getXmlControl()).thenReturn("control");
        when(mock.getXmlHead()).thenReturn("head");
        Map<String, String> issueExtraMap = new HashMap<String, String>();
        issueExtraMap.put("abc", "def");
        issueExtraMap.put("ghk", "lmn");
        when(mock.getIssueExtraMap()).thenReturn(issueExtraMap);

        // 送信用XMLデータ
        String actual = target.createIssuesXmlAsString(mock);

        // 送信用XMLデータの検証
        InputStream bis = new ByteArrayInputStream(actual.getBytes());
        DocumentBuilder db = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = db.parse(bis);
        XPath xpath = XPathFactory.newInstance().newXPath();

        assertThat(xpath.evaluate("/issue/project_id/text()", doc), is("10000"));
        assertThat(xpath.evaluate("/issue/tracker_id/text()", doc), is("20000"));
        assertThat(xpath.evaluate("/issue/xml_control/text()", doc),
                is("control"));
        assertThat(xpath.evaluate("/issue/xml_head/text()", doc), is("head"));
        assertThat(xpath.evaluate("/issue/xml_body/text()", doc), is("body"));
        assertThat(xpath.evaluate("/issue/abc/text()", doc), is("def"));
        assertThat(xpath.evaluate("/issue/ghk/text()", doc), is("lmn"));
    }

    @Test
    public void 訓練モード指定() throws Exception {
        // モード=1を指定
        JmaXmlDataHandler target = new JmaXmlDataHandler(1);

        // Mockオブジェクトに値を設定
        JmaXmlDataParser mock = Mockito.mock(JmaXmlDataParser.class);
        when(mock.getTrackerId()).thenReturn("1");
        when(mock.getXmlBody()).thenReturn("body");
        when(mock.getXmlControl()).thenReturn("control");
        when(mock.getXmlHead()).thenReturn("head");
        Map<String, String> issueExtraMap = new HashMap<String, String>();
        issueExtraMap.put("abc", "def");
        issueExtraMap.put("ghk", "lmn");
        when(mock.getIssueExtraMap()).thenReturn(issueExtraMap);

        // 訓練のプロジェクトID
        String projectId = ParserConfig.getInstance().getTrainingProjectId();
        String actual = target.createIssuesXmlAsString(mock);

        // 送信用XMLデータの検証
        InputStream bis = new ByteArrayInputStream(actual.getBytes());
        DocumentBuilder db = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = db.parse(bis);
        XPath xpath = XPathFactory.newInstance().newXPath();

        assertThat(xpath.evaluate("/issue/project_id/text()", doc),
                is(projectId));
        assertThat(xpath.evaluate("/issue/tracker_id/text()", doc), is("1"));
        assertThat(xpath.evaluate("/issue/xml_control/text()", doc),
                is("control"));
        assertThat(xpath.evaluate("/issue/xml_head/text()", doc), is("head"));
        assertThat(xpath.evaluate("/issue/xml_body/text()", doc), is("body"));
        assertThat(xpath.evaluate("/issue/abc/text()", doc), is("def"));
        assertThat(xpath.evaluate("/issue/ghk/text()", doc), is("lmn"));
    }

    @Test
    public void 試験モード指定() throws ParserConfigurationException,
            XPathExpressionException, SAXException, IOException {
        // モード=2を指定
        JmaXmlDataHandler target = new JmaXmlDataHandler(2);

        // Mockオブジェクトに値を設定
        JmaXmlDataParser mock = Mockito.mock(JmaXmlDataParser.class);
        when(mock.getTrackerId()).thenReturn("1");
        when(mock.getXmlBody()).thenReturn("body");
        when(mock.getXmlControl()).thenReturn("control");
        when(mock.getXmlHead()).thenReturn("head");
        Map<String, String> issueExtraMap = new HashMap<String, String>();
        issueExtraMap.put("abc", "def");
        issueExtraMap.put("ghk", "lmn");
        when(mock.getIssueExtraMap()).thenReturn(issueExtraMap);

        // 試験のプロジェクトID
        String projectId = ParserConfig.getInstance().getTestProjectId();
        String actual = target.createIssuesXmlAsString(mock);

        // 送信用XMLデータの検証
        InputStream bis = new ByteArrayInputStream(actual.getBytes());
        DocumentBuilder db = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = db.parse(bis);
        XPath xpath = XPathFactory.newInstance().newXPath();

        assertThat(xpath.evaluate("/issue/project_id/text()", doc),
                is(projectId));
        assertThat(xpath.evaluate("/issue/tracker_id/text()", doc), is("1"));
        assertThat(xpath.evaluate("/issue/xml_control/text()", doc),
                is("control"));
        assertThat(xpath.evaluate("/issue/xml_head/text()", doc), is("head"));
        assertThat(xpath.evaluate("/issue/xml_body/text()", doc), is("body"));
        assertThat(xpath.evaluate("/issue/abc/text()", doc), is("def"));
        assertThat(xpath.evaluate("/issue/ghk/text()", doc), is("lmn"));
    }

}
