package jp.lg.ishinomaki.city.mrs.pickup;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class JmaXmlDataHandlerTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ParserConfig config = ParserConfig.getInstance();
        config.loadYml("test/config/parser_JmaXmlDataHandlerTest.yml");
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
        when(mock.isAutoLaunch()).thenReturn(false); // プロジェクト自動立ち上げOFF
        when(mock.isAutoSend()).thenReturn(false); // プロジェクト自動送信OFF
        when(mock.getProjectId()).thenReturn("10000");
        when(mock.getTrackerId()).thenReturn("20000");
        when(mock.getXmlBody()).thenReturn("body");
        when(mock.getXmlControl()).thenReturn("control");
        when(mock.getXmlHead()).thenReturn("head");
        Map<String, String> issueExtraMap = new HashMap<String, String>();
        issueExtraMap.put("abc", "def");
        issueExtraMap.put("ghk", "lmn");
        when(mock.getIssueExtraMap()).thenReturn(issueExtraMap);
        List<Map<String, String>> issueGeographyMaps = new ArrayList<Map<String, String>>();
        Map<String, String> geoMap1 = new HashMap<String, String>();
        geoMap1.put("geo1", "日本");
        Map<String, String> geoMap2 = new HashMap<String, String>();
        geoMap2.put("geo1", "アメリカ");
        Map<String, String> geoMap3 = new HashMap<String, String>();
        geoMap3.put("geo2", "フランス");
        Map<String, String> geoMap4 = new HashMap<String, String>();
        geoMap4.put("geo2", "カナダ");
        issueGeographyMaps.add(geoMap1);
        issueGeographyMaps.add(geoMap2);
        issueGeographyMaps.add(geoMap3);
        issueGeographyMaps.add(geoMap4);
        when(mock.getIssueGeographyMaps()).thenReturn(issueGeographyMaps);

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
        assertThat(xpath.evaluate("/issue/xml_control/text()", doc), is("control"));
        assertThat(xpath.evaluate("/issue/xml_head/text()", doc), is("head"));
        assertThat(xpath.evaluate("/issue/xml_body/text()", doc), is("body"));
        assertThat(xpath.evaluate("/issue/abc/text()", doc), is("def"));
        assertThat(xpath.evaluate("/issue/ghk/text()", doc), is("lmn"));
        NodeList geoNodes = (NodeList)xpath.evaluate("/issue/issue_geographies/issue_geography/geo1/text()", doc, XPathConstants.NODESET);
        assertThat(geoNodes.item(0).getNodeValue(), is("日本"));
        assertThat(geoNodes.item(1).getNodeValue(), is("アメリカ"));
        NodeList geoNodes2 = (NodeList)xpath.evaluate("/issue/issue_geographies/issue_geography/geo2/text()", doc, XPathConstants.NODESET);
        assertThat(geoNodes2.item(0).getNodeValue(), is("フランス"));
        assertThat(geoNodes2.item(1).getNodeValue(), is("カナダ"));
    }

    @Test
    public void 訓練モード指定() throws Exception {
        // モード=1を指定
        JmaXmlDataHandler target = new JmaXmlDataHandler(1);

        // Mockオブジェクトに値を設定
        JmaXmlDataParser mock = Mockito.mock(JmaXmlDataParser.class);
        when(mock.isAutoLaunch()).thenReturn(false); // プロジェクト自動立ち上げOFF
        when(mock.isAutoSend()).thenReturn(false); // プロジェクト自動送信OFF
        when(mock.getTrackerId()).thenReturn("1");
        when(mock.getXmlBody()).thenReturn("body");
        when(mock.getXmlControl()).thenReturn("control");
        when(mock.getXmlHead()).thenReturn("head");
        Map<String, String> issueExtraMap = new HashMap<String, String>();
        issueExtraMap.put("abc", "def");
        issueExtraMap.put("ghk", "lmn");
        when(mock.getIssueExtraMap()).thenReturn(issueExtraMap);
        List<Map<String, String>> issueGeographyMaps = new ArrayList<Map<String, String>>();
        Map<String, String> geoMap1 = new HashMap<String, String>();
        geoMap1.put("geo1", "日本");
        Map<String, String> geoMap2 = new HashMap<String, String>();
        geoMap2.put("geo1", "アメリカ");
        Map<String, String> geoMap3 = new HashMap<String, String>();
        geoMap3.put("geo2", "フランス");
        Map<String, String> geoMap4 = new HashMap<String, String>();
        geoMap4.put("geo2", "カナダ");
        issueGeographyMaps.add(geoMap1);
        issueGeographyMaps.add(geoMap2);
        issueGeographyMaps.add(geoMap3);
        issueGeographyMaps.add(geoMap4);
        when(mock.getIssueGeographyMaps()).thenReturn(issueGeographyMaps);

        // 訓練のプロジェクトID
        String projectId = ParserConfig.getInstance().getTrainingProjectId();
        String actual = target.createIssuesXmlAsString(mock);
        
        // 送信用XMLデータの検証
        InputStream bis = new ByteArrayInputStream(actual.getBytes());
        DocumentBuilder db = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = db.parse(bis);
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        assertThat(xpath.evaluate("/issue/project_id/text()", doc), is(projectId));
        assertThat(xpath.evaluate("/issue/tracker_id/text()", doc), is("1"));
        assertThat(xpath.evaluate("/issue/xml_control/text()", doc), is("control"));
        assertThat(xpath.evaluate("/issue/xml_head/text()", doc), is("head"));
        assertThat(xpath.evaluate("/issue/xml_body/text()", doc), is("body"));
        assertThat(xpath.evaluate("/issue/abc/text()", doc), is("def"));
        assertThat(xpath.evaluate("/issue/ghk/text()", doc), is("lmn"));
        NodeList geoNodes = (NodeList)xpath.evaluate("/issue/issue_geographies/issue_geography/geo1/text()", doc, XPathConstants.NODESET);
        assertThat(geoNodes.item(0).getNodeValue(), is("日本"));
        assertThat(geoNodes.item(1).getNodeValue(), is("アメリカ"));
        NodeList geoNodes2 = (NodeList)xpath.evaluate("/issue/issue_geographies/issue_geography/geo2/text()", doc, XPathConstants.NODESET);
        assertThat(geoNodes2.item(0).getNodeValue(), is("フランス"));
        assertThat(geoNodes2.item(1).getNodeValue(), is("カナダ"));
    }

    @Test
    public void 試験モード指定() throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
        // モード=2を指定
        JmaXmlDataHandler target = new JmaXmlDataHandler(2);

        // Mockオブジェクトに値を設定
        JmaXmlDataParser mock = Mockito.mock(JmaXmlDataParser.class);
        when(mock.isAutoLaunch()).thenReturn(false); // プロジェクト自動立ち上げOFF
        when(mock.isAutoSend()).thenReturn(false); // プロジェクト自動送信OFF
        when(mock.getTrackerId()).thenReturn("1");
        when(mock.getXmlBody()).thenReturn("body");
        when(mock.getXmlControl()).thenReturn("control");
        when(mock.getXmlHead()).thenReturn("head");
        Map<String, String> issueExtraMap = new HashMap<String, String>();
        issueExtraMap.put("abc", "def");
        issueExtraMap.put("ghk", "lmn");
        when(mock.getIssueExtraMap()).thenReturn(issueExtraMap);
        List<Map<String, String>> issueGeographyMaps = new ArrayList<Map<String, String>>();
        Map<String, String> geoMap1 = new HashMap<String, String>();
        geoMap1.put("geo1", "日本");
        Map<String, String> geoMap2 = new HashMap<String, String>();
        geoMap2.put("geo1", "アメリカ");
        Map<String, String> geoMap3 = new HashMap<String, String>();
        geoMap3.put("geo2", "フランス");
        Map<String, String> geoMap4 = new HashMap<String, String>();
        geoMap4.put("geo2", "カナダ");
        issueGeographyMaps.add(geoMap1);
        issueGeographyMaps.add(geoMap2);
        issueGeographyMaps.add(geoMap3);
        issueGeographyMaps.add(geoMap4);
        when(mock.getIssueGeographyMaps()).thenReturn(issueGeographyMaps);

        // 試験のプロジェクトID
        String projectId = ParserConfig.getInstance().getTestProjectId();
        String actual = target.createIssuesXmlAsString(mock);
        
        // 送信用XMLデータの検証
        InputStream bis = new ByteArrayInputStream(actual.getBytes());
        DocumentBuilder db = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = db.parse(bis);
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        assertThat(xpath.evaluate("/issue/project_id/text()", doc), is(projectId));
        assertThat(xpath.evaluate("/issue/tracker_id/text()", doc), is("1"));
        assertThat(xpath.evaluate("/issue/xml_control/text()", doc), is("control"));
        assertThat(xpath.evaluate("/issue/xml_head/text()", doc), is("head"));
        assertThat(xpath.evaluate("/issue/xml_body/text()", doc), is("body"));
        assertThat(xpath.evaluate("/issue/abc/text()", doc), is("def"));
        assertThat(xpath.evaluate("/issue/ghk/text()", doc), is("lmn"));
        NodeList geoNodes = (NodeList)xpath.evaluate("/issue/issue_geographies/issue_geography/geo1/text()", doc, XPathConstants.NODESET);
        assertThat(geoNodes.item(0).getNodeValue(), is("日本"));
        assertThat(geoNodes.item(1).getNodeValue(), is("アメリカ"));
        NodeList geoNodes2 = (NodeList)xpath.evaluate("/issue/issue_geographies/issue_geography/geo2/text()", doc, XPathConstants.NODESET);
        assertThat(geoNodes2.item(0).getNodeValue(), is("フランス"));
        assertThat(geoNodes2.item(1).getNodeValue(), is("カナダ"));
    }

    @Test
    public void プロジェクト自動立ち上げと自動送信あり() throws Exception {
        JmaXmlDataHandler target = new JmaXmlDataHandler();

        // Mockオブジェクトに値を設定
        JmaXmlDataParser mock = Mockito.mock(JmaXmlDataParser.class);
        when(mock.isAutoLaunch()).thenReturn(true); // プロジェクト自動立ち上げON
        when(mock.isAutoSend()).thenReturn(true); // プロジェクト自動送信ON
        when(mock.getTrackerId()).thenReturn("1");
        when(mock.getXmlBody()).thenReturn("body");
        when(mock.getXmlControl()).thenReturn("control");
        when(mock.getXmlHead()).thenReturn("head");
        Map<String, String> issueExtraMap = new HashMap<String, String>();
        issueExtraMap.put("abc", "def");
        issueExtraMap.put("ghk", "lmn");
        when(mock.getIssueExtraMap()).thenReturn(issueExtraMap);
        List<Map<String, String>> issueGeographyMaps = new ArrayList<Map<String, String>>();
        Map<String, String> geoMap1 = new HashMap<String, String>();
        geoMap1.put("geo1", "日本");
        Map<String, String> geoMap2 = new HashMap<String, String>();
        geoMap2.put("geo1", "アメリカ");
        Map<String, String> geoMap3 = new HashMap<String, String>();
        geoMap3.put("geo2", "フランス");
        Map<String, String> geoMap4 = new HashMap<String, String>();
        geoMap4.put("geo2", "カナダ");
        issueGeographyMaps.add(geoMap1);
        issueGeographyMaps.add(geoMap2);
        issueGeographyMaps.add(geoMap3);
        issueGeographyMaps.add(geoMap4);
        when(mock.getIssueGeographyMaps()).thenReturn(issueGeographyMaps);

        String actual = target.createIssuesXmlAsString(mock);
        
        // 送信用XMLデータの検証
        InputStream bis = new ByteArrayInputStream(actual.getBytes());
        DocumentBuilder db = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = db.parse(bis);
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        assertThat(xpath.evaluate("/issue/auto_launch/text()", doc), is("1"));
        assertThat(xpath.evaluate("/issue/auto_send/text()", doc), is("1"));
        assertThat(xpath.evaluate("/issue/tracker_id/text()", doc), is("1"));
        assertThat(xpath.evaluate("/issue/xml_control/text()", doc), is("control"));
        assertThat(xpath.evaluate("/issue/xml_head/text()", doc), is("head"));
        assertThat(xpath.evaluate("/issue/xml_body/text()", doc), is("body"));
        assertThat(xpath.evaluate("/issue/abc/text()", doc), is("def"));
        assertThat(xpath.evaluate("/issue/ghk/text()", doc), is("lmn"));
        NodeList geoNodes = (NodeList)xpath.evaluate("/issue/issue_geographies/issue_geography/geo1/text()", doc, XPathConstants.NODESET);
        assertThat(geoNodes.item(0).getNodeValue(), is("日本"));
        assertThat(geoNodes.item(1).getNodeValue(), is("アメリカ"));
        NodeList geoNodes2 = (NodeList)xpath.evaluate("/issue/issue_geographies/issue_geography/geo2/text()", doc, XPathConstants.NODESET);
        assertThat(geoNodes2.item(0).getNodeValue(), is("フランス"));
        assertThat(geoNodes2.item(1).getNodeValue(), is("カナダ"));
        
        // プロジェクトIDは時刻を含む自動生成文字列のため存在のみ確認
        assertThat(xpath.evaluate("/issue/project_id/text()", doc), is(notNullValue()));

    }

    @Test
    public void プロジェクト自動立ち上げと自動送信なし() throws Exception {
        JmaXmlDataHandler target = new JmaXmlDataHandler();

        // Mockオブジェクトに値を設定
        JmaXmlDataParser mock = Mockito.mock(JmaXmlDataParser.class);
        when(mock.isAutoLaunch()).thenReturn(false); // プロジェクト自動立ち上げON
        when(mock.isAutoSend()).thenReturn(false); // プロジェクト自動送信ON
        when(mock.getProjectId()).thenReturn("10000");
        when(mock.getTrackerId()).thenReturn("20000");
        when(mock.getXmlBody()).thenReturn("body");
        when(mock.getXmlControl()).thenReturn("control");
        when(mock.getXmlHead()).thenReturn("head");
        Map<String, String> issueExtraMap = new HashMap<String, String>();
        issueExtraMap.put("abc", "def");
        issueExtraMap.put("ghk", "lmn");
        when(mock.getIssueExtraMap()).thenReturn(issueExtraMap);
        List<Map<String, String>> issueGeographyMaps = new ArrayList<Map<String, String>>();
        Map<String, String> geoMap1 = new HashMap<String, String>();
        geoMap1.put("geo1", "日本");
        Map<String, String> geoMap2 = new HashMap<String, String>();
        geoMap2.put("geo1", "アメリカ");
        Map<String, String> geoMap3 = new HashMap<String, String>();
        geoMap3.put("geo2", "フランス");
        Map<String, String> geoMap4 = new HashMap<String, String>();
        geoMap4.put("geo2", "カナダ");
        issueGeographyMaps.add(geoMap1);
        issueGeographyMaps.add(geoMap2);
        issueGeographyMaps.add(geoMap3);
        issueGeographyMaps.add(geoMap4);
        when(mock.getIssueGeographyMaps()).thenReturn(issueGeographyMaps);

        String actual = target.createIssuesXmlAsString(mock);

        // 送信用XMLデータの検証
        InputStream bis = new ByteArrayInputStream(actual.getBytes());
        DocumentBuilder db = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = db.parse(bis);
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        assertThat(xpath.evaluate("/issue/auto_launch/text()", doc), is(""));
        assertThat(xpath.evaluate("/issue/auto_send/text()", doc), is(""));
        assertThat(xpath.evaluate("/issue/project_id/text()", doc), is("10000"));
        assertThat(xpath.evaluate("/issue/tracker_id/text()", doc), is("20000"));
        assertThat(xpath.evaluate("/issue/xml_control/text()", doc), is("control"));
        assertThat(xpath.evaluate("/issue/xml_head/text()", doc), is("head"));
        assertThat(xpath.evaluate("/issue/xml_body/text()", doc), is("body"));
        assertThat(xpath.evaluate("/issue/abc/text()", doc), is("def"));
        assertThat(xpath.evaluate("/issue/ghk/text()", doc), is("lmn"));
        NodeList geoNodes = (NodeList)xpath.evaluate("/issue/issue_geographies/issue_geography/geo1/text()", doc, XPathConstants.NODESET);
        assertThat(geoNodes.item(0).getNodeValue(), is("日本"));
        assertThat(geoNodes.item(1).getNodeValue(), is("アメリカ"));
        NodeList geoNodes2 = (NodeList)xpath.evaluate("/issue/issue_geographies/issue_geography/geo2/text()", doc, XPathConstants.NODESET);
        assertThat(geoNodes2.item(0).getNodeValue(), is("フランス"));
        assertThat(geoNodes2.item(1).getNodeValue(), is("カナダ"));
    }

}
