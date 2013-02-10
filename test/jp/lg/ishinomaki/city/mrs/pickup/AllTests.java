package jp.lg.ishinomaki.city.mrs.pickup;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ PickupThreadTest.class, JmaXmlDataHandlerTest.class,
        KsnXmlDataHandlerTest.class, PdfDataHandlerTest.class,
        TarDataHandlerTest.class, TextDataHandlerTest.class })
public class AllTests {

}
