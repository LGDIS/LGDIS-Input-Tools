package jp.lg.ishinomaki.city.mrs.receiver;

import jp.lg.ishinomaki.city.mrs.receiver.jma.JmaMessageTest;
import jp.lg.ishinomaki.city.mrs.receiver.jma.JmaServerSocketControlTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ JmaMessageTest.class, JmaServerSocketControlTest.class,
        ReceiverDataAnalyzeTaskTest.class, ReceiverThreadManagerTest.class,
        ReceiverThreadTest.class })
public class AllTests {

}
