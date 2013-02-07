package jp.lg.ishinomaki.city.mrs.pickup;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.lg.ishinomaki.city.mrs.parser.JmaXmlDataParser;
import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

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
    public void 通常モード指定() {
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
        issueExtraMap.put("012", "345");
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
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<issue><project_id>10000</project_id><tracker_id>20000</tracker_id><xml_control><![CDATA[control]]></xml_control><xml_head><![CDATA[head]]></xml_head><xml_body><![CDATA[body]]></xml_body><abc>def</abc><012>345</012><issue_geographies type=\"array\"><issue_geography><geo1>日本</geo1></issue_geography><issue_geography><geo1>アメリカ</geo1></issue_geography><issue_geography><geo2>フランス</geo2></issue_geography><issue_geography><geo2>カナダ</geo2></issue_geography></issue_geographies></issue>";
        assertThat(actual, is(expected));
    }

    @Test
    public void 訓練モード指定() {
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
        issueExtraMap.put("012", "345");
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
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<issue><project_id>"
                + projectId
                + "</project_id><tracker_id>1</tracker_id><xml_control><![CDATA[control]]></xml_control><xml_head><![CDATA[head]]></xml_head><xml_body><![CDATA[body]]></xml_body><abc>def</abc><012>345</012><issue_geographies type=\"array\"><issue_geography><geo1>日本</geo1></issue_geography><issue_geography><geo1>アメリカ</geo1></issue_geography><issue_geography><geo2>フランス</geo2></issue_geography><issue_geography><geo2>カナダ</geo2></issue_geography></issue_geographies></issue>";
        assertThat(actual, is(expected));
    }

    @Test
    public void 試験モード指定() {
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
        issueExtraMap.put("012", "345");
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
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<issue><project_id>"
                + projectId
                + "</project_id><tracker_id>1</tracker_id><xml_control><![CDATA[control]]></xml_control><xml_head><![CDATA[head]]></xml_head><xml_body><![CDATA[body]]></xml_body><abc>def</abc><012>345</012><issue_geographies type=\"array\"><issue_geography><geo1>日本</geo1></issue_geography><issue_geography><geo1>アメリカ</geo1></issue_geography><issue_geography><geo2>フランス</geo2></issue_geography><issue_geography><geo2>カナダ</geo2></issue_geography></issue_geographies></issue>";
        assertThat(actual, is(expected));
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
        issueExtraMap.put("012", "345");
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
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<issue><auto_launch>1</auto_launch><auto_send>1</auto_send><tracker_id>1</tracker_id><xml_control><![CDATA[control]]></xml_control><xml_head><![CDATA[head]]></xml_head><xml_body><![CDATA[body]]></xml_body><abc>def</abc><012>345</012><issue_geographies type=\"array\"><issue_geography><geo1>日本</geo1></issue_geography><issue_geography><geo1>アメリカ</geo1></issue_geography><issue_geography><geo2>フランス</geo2></issue_geography><issue_geography><geo2>カナダ</geo2></issue_geography></issue_geographies></issue>";
        assertThat(actual, is(expected));
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
        issueExtraMap.put("012", "345");
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
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<issue><project_id>10000</project_id><tracker_id>20000</tracker_id><xml_control><![CDATA[control]]></xml_control><xml_head><![CDATA[head]]></xml_head><xml_body><![CDATA[body]]></xml_body><abc>def</abc><012>345</012><issue_geographies type=\"array\"><issue_geography><geo1>日本</geo1></issue_geography><issue_geography><geo1>アメリカ</geo1></issue_geography><issue_geography><geo2>フランス</geo2></issue_geography><issue_geography><geo2>カナダ</geo2></issue_geography></issue_geographies></issue>";
        assertThat(actual, is(expected));
    }

}
