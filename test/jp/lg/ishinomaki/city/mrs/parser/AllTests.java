package jp.lg.ishinomaki.city.mrs.parser;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ JmaParseRuleTest.class, JmaXmlDataParserTest.class,
        KsnParseRuleTest.class, XmlSchemaCheckerTest.class })
public class AllTests {

}
