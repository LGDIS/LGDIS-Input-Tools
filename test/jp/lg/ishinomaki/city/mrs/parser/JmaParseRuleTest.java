package jp.lg.ishinomaki.city.mrs.parser;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JmaParseRuleTest {

    static JmaParseRule rule;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ParserConfig config = ParserConfig.getInstance();
        config.loadYml("test/config/parser_JmaParseRuleTest.yml");
        rule = JmaParseRule.getInstance();
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
    public void プロジェクト自動立ち上げの震度しきい値() {
        String actual = rule.getSeismicIntensityThreashold();
        assertThat(actual, is("5+"));
    }

    @Test
    public void デフォルトのプロジェクトID() {
        String actual = rule.getDefaultProjectId();
        assertThat(actual, is("I04202000000000000001"));
    }

    @Test
    public void 震度取得用のXPath() {
        String actual = rule.getAutoLaunchSeismicIntensityXpath();
        assertThat(
                actual,
                is("/Report/Body/Intensity/Observation/Pref/Area/City[Name/text()=\"石巻市\"]/MaxInt/text()"));
    }

    @Test
    public void トラッカーID() {
        String actual = rule.getTrackerId("JMA", "津波警報・注意報・予報a");
        assertThat(actual, is("3"));
    }

    @Test
    public void トラッカー取得用のXPath() {
        String actual = rule.getTrackerXpath("JMA");
        assertThat(actual, is("/Report/Control/Title/text()"));
    }

    @Test
    public void 津波高さ取得用のXPath() {
        String actual = rule.getAutoLaunchTsunamiHeightXpath();
        assertThat(
                actual,
                is("/Report/Body/Tsunami/Estimation/Item[Area/Name/text()=\"宮城金華山沖\"]/MaxHeight/TsunamiHeight/text()"));
    }

    @Test
    public void Body部取得用のXPath() {
        String actual = rule.getXmlBodyPath();
        assertThat(actual, is("/Report/Body"));
    }

    @Test
    public void Control部取得用のXPath() {
        String actual = rule.getXmlControlPath();
        assertThat(actual, is("/Report/Control"));
    }

    @Test
    public void Head部取得用のXPath() {
        String actual = rule.getXmlHeadPath();
        assertThat(actual, is("/Report/Head"));
    }

    @Test
    public void プロジェクト自動立ち上げの津波高さ() {
        Double dactual = rule.getTsunamiHeightThreashold();
        assertThat(dactual, is(2.2));
    }

    @Test
    public void 位置情報関連() {
        List<Map<String, Object>> infos = rule.getCoordinateInfos();
        assertThat(infos, is(notNullValue()));

        Map<String, Object> info = infos.get(0);
        assertThat(info, is(notNullValue()));

        String path = (String) info.get(JmaParseRule.PATH);
        assertThat(
                path,
                is("/Report/Body/MeteorologicalInfos/MeteorologicalInfo/Item/Area/Coordinate[@type=\"中心位置（度）\" or @type=\"実況位置（度）\" or @type=\"１２時間後位置（度）\" or @type=\"２４時間後位置（度）\" or @type=\"位置（度）\" or @type=\"予想位置　１２時間後（度）\" or @type=\"予想位置　２４時間後（度）\"]/text()"));

        String remarks = (String) info.get(JmaParseRule.REMARKS_PATH);
        assertThat(remarks, is("../../Name/text()"));
    }

    @Test
    public void ライン情報関連() {
        List<Map<String, Object>> infos = rule.getLineInfos();
        assertThat(infos, is(notNullValue()));

        Map<String, Object> info = infos.get(0);
        assertThat(info, is(notNullValue()));

        String path = (String) info.get(JmaParseRule.PATH);
        assertThat(
                path,
                is("/Report/Body/MeteorologicalInfos/MeteorologicalInfo/Item/Area/Line[@type=\"位置（度）\" or @type=\"前線（度）\"]/text()"));

        String remarks = (String) info.get(JmaParseRule.REMARKS_PATH);
        assertThat(remarks, is("../../../Kind/Name/text()"));
    }

    @Test
    public void ポリゴン情報関連() {
        List<Map<String, Object>> infos = rule.getPolygonInfos();
        assertThat(infos, is(notNullValue()));

        Map<String, Object> info = infos.get(0);
        assertThat(info, is(notNullValue()));

        String path = (String) info.get(JmaParseRule.PATH);
        assertThat(
                path,
                is("/Report/Body/MeteorologicalInfos/MeteorologicalInfo/Item/Area/Polygon[@type=\"位置（度）\" or @type=\"領域（度）\"]/text()"));

        String remarks = (String) info.get(JmaParseRule.REMARKS_PATH);
        assertThat(remarks, is("../../../Name/text()"));
    }

    @Test
    public void ロケーション情報関連() {
        List<Map<String, Object>> infos = rule.getLocationInfos();
        assertThat(infos, is(notNullValue()));

        Map<String, Object> info = infos.get(0);
        assertThat(info, is(notNullValue()));

        String path = (String) info.get(JmaParseRule.PATH);
        assertThat(
                path,
                is("/Report/Body/MeteorologicalInfos/MeteorologicalInfo/Item/Station/Location/text()"));

        String remarks = (String) info.get(JmaParseRule.REMARKS_PATH);
        assertThat(remarks, is("../../Name/text()"));
    }

    @Test
    public void プロジェクトID() {
        String actual = rule.getProjectId("試験");
        assertThat(actual, is("test-project"));
    }

}
