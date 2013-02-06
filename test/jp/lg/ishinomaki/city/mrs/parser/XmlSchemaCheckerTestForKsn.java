package jp.lg.ishinomaki.city.mrs.parser;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class XmlSchemaCheckerTestForKsn {

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
