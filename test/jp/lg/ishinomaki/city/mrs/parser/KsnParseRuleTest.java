package jp.lg.ishinomaki.city.mrs.parser;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class KsnParseRuleTest {

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
        KsnParseRule rule = KsnParseRule.getInstance();

        String actual = rule.getDefaultProjectId();
        assertThat(actual, is("I04202000000000000001"));

        Map<String, String> issueExtras = rule.getIssueExtras();
        actual = issueExtras.get("xml_control_cause");
        assertThat(actual, is("/WarningReport/@cause"));

        actual = rule.getProjectId("2");
        assertThat(actual, is("training-project"));

        actual = rule.getProjectXpath();
        assertThat(actual, is("/WarningReport/@cause"));

        actual = rule.getTrackerId();
        assertThat(actual, is("6"));

        actual = rule.getXmlBodyPath();
        assertThat(actual, is("/WarningReport/Detail/Text"));

        actual = rule.getXmlHeadPath();
        assertThat(actual, is("/WarningReport/Summary"));

    }

}
