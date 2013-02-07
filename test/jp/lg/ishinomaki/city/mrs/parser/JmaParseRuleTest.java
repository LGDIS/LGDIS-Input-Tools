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

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ParserConfig config = ParserConfig.getInstance();
        config.loadYml("test/config/parser.yml");
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
    public void 全ての定義内容が正しくロードできていることを確認() {
        JmaParseRule rule = JmaParseRule.getInstance();

        String actual = rule.getAutoLaunchSeismicIntensityThreashold();
        assertThat(actual, is("5+"));

        actual = rule.getAutoSendSeismicIntensityThreashold();
        assertThat(actual, is("6-"));

        actual = rule.getDefaultProjectId();
        assertThat(actual, is("I04202000000000000001"));

        actual = rule.getDefaultTrackerId();
        assertThat(actual, is("4"));

        actual = rule.getSeismicIntensityXpath();
        assertThat(
                actual,
                is("/Report/Body/Intensity/Observation/Pref/Area/City[Name/text()=\"石巻市\"]/MaxInt/text()"));

        actual = rule.getTrackerId("津波警報・注意報・予報a");
        assertThat(actual, is("3"));

        actual = rule.getTrackerXpath();
        assertThat(actual, is("/Report/Control/Title/text()"));

        actual = rule.getTsunamiHeightXpath();
        assertThat(
                actual,
                is("/Report/Body/Tsunami/Estimation/Item[Arear/Name/text()=\"宮城金華山沖\"]/MaxHeight/TsunamiHeight/text()"));

        actual = rule.getXmlBodyPath();
        assertThat(actual, is("/Report/Body"));

        actual = rule.getXmlControlPath();
        assertThat(actual, is("/Report/Control"));

        actual = rule.getXmlHeadPath();
        assertThat(actual, is("/Report/Head"));

        Double dactual = rule.getAutoLaunchTsunamiHeightThreashold();
        assertThat(dactual, is(2.2));

        dactual = rule.getAutoSendTsunamiHeightThreashold();
        assertThat(dactual, is(1.7));
        
        List<Map<String, Object>> cinfos = rule.getCoordinateInfos();
        assertThat(cinfos, is(notNullValue()));
        
        Map<String, Object> cinfo = cinfos.get(0);
        assertThat(cinfo, is(notNullValue()));
        
        String cpath = (String)cinfo.get(JmaParseRule.PATH);
        assertThat(cpath, is("/Report/Body/MeteorologicalInfos/MeteorologicalInfo/Item/Area/Coordinate[@type=\"中心位置（度）\" or @type=\"実況位置（度）\" or @type=\"実況位置（度分）\" or @type=\"１２時間後位置（度）\" or @type=\"２４時間後位置（度）\" or @type=\"位置（度）\" or @type=\"予想位置　１２時間後（度）\" or @type=\"予想位置　２４時間後（度）\"]/text()"));
        
        List<String> cremarks = (List<String>)cinfo.get(JmaParseRule.REMARKS_PATHS);
        assertThat(cremarks.get(0), is("../../Name/text()"));
        
        actual = rule.getProjectId("試験");
        assertThat(actual, is("test-project"));
        
    }

}
