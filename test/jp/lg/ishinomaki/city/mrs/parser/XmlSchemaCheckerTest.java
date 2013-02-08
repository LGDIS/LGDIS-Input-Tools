package jp.lg.ishinomaki.city.mrs.parser;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class XmlSchemaCheckerTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // 構成ファイル読み込み
        ParserConfig config = ParserConfig.getInstance();
        try {
            config.loadYml("config/parser.yml");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
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
    public void インスタンスを生成() {
        XmlSchemaChecker checker = XmlSchemaChecker.getInstatnce(ParserConfig
                .getInstance().getJmaSchemaFilePath());
        assertThat(checker, is(notNullValue()));
    }

    @Test
    public void JMAXMLバリデート結果OK() {
        XmlSchemaChecker checker = XmlSchemaChecker.getInstatnce(ParserConfig
                .getInstance().getJmaSchemaFilePath());
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Control><Title>季節観測</Title><DateTime>2009-01-09T02:02:05Z</DateTime><Status>通常</Status><EditorialOffice>熊谷地方気象台</EditorialOffice><PublishingOffice>熊谷地方気象台</PublishingOffice></Control><Head xmlns=\"http://xml.kishou.go.jp/jmaxml1/informationBasis1/\"><Title>季節観測</Title><ReportDateTime>2009-01-09T11:00:00+09:00</ReportDateTime><TargetDateTime>2009-01-09T00:00:00+09:00</TargetDateTime><EventID>20090109110000_初雪</EventID><InfoType>発表</InfoType><Serial /><InfoKind>特殊気象報</InfoKind><InfoKindVersion>1.0_0</InfoKindVersion><Headline><Text /></Headline></Head><Body xmlns=\"http://xml.kishou.go.jp/jmaxml1/body/meteorology1/\"><MeteorologicalInfos type=\"季節観測\"><MeteorologicalInfo><DateTime significant=\"yyyy-mm-dd\">2009-01-09T00:00:00+09:00</DateTime><Item><Kind><Name>初雪</Name></Kind><Station><Name>熊谷地方気象台</Name><Code type=\"国際地点番号\">47626</Code><Location>熊谷市桜町</Location></Station></Item></MeteorologicalInfo></MeteorologicalInfos><AdditionalInfo><ObservationAddition><DeviationFromNormal>-9</DeviationFromNormal><DeviationFromLastYear>7</DeviationFromLastYear></ObservationAddition></AdditionalInfo></Body></Report>";
        assertThat(checker.validate(xml), is(true));
    }

    @Test
    public void JMAXMLバリデート結果NG() {
        XmlSchemaChecker checker = XmlSchemaChecker.getInstatnce(ParserConfig
                .getInstance().getJmaSchemaFilePath());
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Report xmlns=\"http://xml.kishou.go.jp/jmaxml1/\" xmlns:jmx=\"http://xml.kishou.go.jp/jmaxml1/\"><Control><Title>季節観測</Title><DateTime>2009-01-09T02:02:05Z</DateTime><Status>異常</Status><EditorialOffice>熊谷地方気象台</EditorialOffice><PublishingOffice>熊谷地方気象台</PublishingOffice></Control><Head xmlns=\"http://xml.kishou.go.jp/jmaxml1/informationBasis1/\"><Title>季節観測</Title><ReportDateTime>2009-01-09T11:00:00+09:00</ReportDateTime><TargetDateTime>2009-01-09T00:00:00+09:00</TargetDateTime><EventID>20090109110000_初雪</EventID><InfoType>発表</InfoType><Serial /><InfoKind>特殊気象報</InfoKind><InfoKindVersion>1.0_0</InfoKindVersion><Headline><Text /></Headline></Head><Body xmlns=\"http://xml.kishou.go.jp/jmaxml1/body/meteorology1/\"><MeteorologicalInfos type=\"季節観測\"><MeteorologicalInfo><DateTime significant=\"yyyy-mm-dd\">2009-01-09T00:00:00+09:00</DateTime><Item><Kind><Name>初雪</Name></Kind><Station><Name>熊谷地方気象台</Name><Code type=\"国際地点番号\">47626</Code><Location>熊谷市桜町</Location></Station></Item></MeteorologicalInfo></MeteorologicalInfos><AdditionalInfo><ObservationAddition><DeviationFromNormal>-9</DeviationFromNormal><DeviationFromLastYear>7</DeviationFromLastYear></ObservationAddition></AdditionalInfo></Body></Report>";
        assertThat(checker.validate(xml), is(false));
    }
    
    @Test
    public void 河川XMLバリデート結果OK() {
        XmlSchemaChecker checker = XmlSchemaChecker.getInstatnce(ParserConfig
                .getInstance().getKsnSchemaFilePath());
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WarningReport cause=\"0\" apply=\"1\" xsi:schemaLocation=\"http://wrtrpt.unify.river.go.jp wrn_sample.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://wrtrpt.unify.river.go.jp\"><!--水防警報サンプル--><Summary><JursdctnOffice><RvrSys rvrSysCode=\"85053000\"><Name>庄内川水系</Name><River riverCode=\"85053001\"><Name>庄内川</Name></River></RvrSys></JursdctnOffice></Summary></WarningReport>";
        assertThat(checker.validate(xml), is(true));
    }

    @Test
    public void 河川XMLバリデート結果NG() {
        XmlSchemaChecker checker = XmlSchemaChecker.getInstatnce(ParserConfig
                .getInstance().getKsnSchemaFilePath());
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><WarningReport cause=\"0\" apply=\"1\" xsi:schemaLocation=\"http://wrtrpt.unify.river.go.jp wrn_sample.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://wrtrpt.unify.river.go.jp\"><!--水防警報サンプル--><Summary><JursdctnOffice><RvrSys rvrSysCode=\"85053000\"><Name>庄内川水系</Name><River riverCode=\"85053001\"><No>庄内川</No></River></RvrSys></JursdctnOffice></Summary></WarningReport>";
        assertThat(checker.validate(xml), is(false));
    }

}
