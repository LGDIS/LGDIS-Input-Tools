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

    static KsnParseRule rule;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ParserConfig config = ParserConfig.getInstance();
        config.loadYml("test/config/parser_KsnParseRuleTest.yml");
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
    public void デフォルトのプロジェクトID() {
        String actual = rule.getDefaultProjectId();
        assertThat(actual, is("I04202000000000000001"));
    }

    @Test
    public void Issue拡張テーブル() {
        Map<String, String> issueExtras = rule.getIssueExtras();
        String actual = issueExtras.get("xml_control_cause");
        assertThat(actual, is("/WarningReport/@cause"));
    }

    @Test
    public void プロジェクトID() {
        String actual = rule.getProjectId("2");
        assertThat(actual, is("training-project"));
    }

    @Test
    public void プロジェクト取得用のXPath() {
        String actual = rule.getProjectXpath();
        assertThat(actual, is("/WarningReport/@cause"));
    }

    @Test
    public void トラッカーID() {
        String actual = rule.getTrackerId();
        assertThat(actual, is("6"));
    }

    @Test
    public void Body部取得用XPath() {
        String actual = rule.getXmlBodyPath();
        assertThat(actual, is("/WarningReport/Detail/Text"));
    }

    @Test
    public void Head部取得用XPath() {
        String actual = rule.getXmlHeadPath();
        assertThat(actual, is("/WarningReport/Summary"));
    }

}
