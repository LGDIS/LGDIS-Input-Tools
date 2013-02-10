package jp.lg.ishinomaki.city.mrs.utils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ArchiveUtilsTest.class, BCHChecksumHelperTest.class,
        FileUtilsTest.class, StringUtilsTest.class })
public class AllTests {

}
