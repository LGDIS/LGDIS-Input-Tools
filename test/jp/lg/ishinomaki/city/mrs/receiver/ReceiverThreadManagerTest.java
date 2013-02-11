package jp.lg.ishinomaki.city.mrs.receiver;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import jp.lg.ishinomaki.city.mrs.analyzer.CabinetDataAnalyzer;
import jp.lg.ishinomaki.city.mrs.analyzer.DataAnalyzer;
import jp.lg.ishinomaki.city.mrs.analyzer.JmaDataAnalyzer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReceiverThreadManagerTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ReceiverConfig.getInstance().loadYml(
                "test/config/receiver_ReceiverThreadManagerTest.yml");
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
    public void インスタンス生成() throws Exception {
        ReceiverThreadManager target = new ReceiverThreadManager();

        // スレッド数は3つ
        assertThat(3, is(target.threadManager.size()));

        // スレッド定義の内容を確認
        ReceiverThread thread1 = target.threadManager
                .get("JAlert-Meteorological");
        assertThat(thread1.threadName, is("JAlert-Meteorological"));
        assertThat(thread1.outputPath, is("/Users/igakuratakayuki/JmaOutput"));
        assertThat(thread1.inputId, is("JAL"));
        assertThat(thread1.mode, is(0));
        DataAnalyzer analyzer1 = thread1.analyzers.get("BI");
        assertThat(analyzer1, is(instanceOf(JmaDataAnalyzer.class)));

        ReceiverThread thread2 = target.threadManager.get("JAlert-Cabinet");
        assertThat(thread2.threadName, is("JAlert-Cabinet"));
        assertThat(thread2.outputPath, is("/Users/igakuratakayuki/JmaOutput"));
        assertThat(thread2.inputId, is("JAL"));
        assertThat(thread2.mode, is(0));
        DataAnalyzer analyzer2 = thread2.analyzers.get("JL");
        assertThat(analyzer2, is(instanceOf(CabinetDataAnalyzer.class)));

        ReceiverThread thread3 = target.threadManager.get("JMA");
        assertThat(thread3.threadName, is("JMA"));
        assertThat(thread3.outputPath, is("/Users/igakuratakayuki/JmaOutput"));
        assertThat(thread3.inputId, is("JMA"));
        assertThat(thread3.mode, is(2));
        DataAnalyzer analyzer3 = thread3.analyzers.get("BI");
        assertThat(analyzer3, is(instanceOf(JmaDataAnalyzer.class)));
    }

    @Test
    public void スレッドの開始と停止を確認() throws Exception {

        ReceiverThreadManager target = new ReceiverThreadManager();
        ReceiverThread thread1 = target.threadManager
                .get("JAlert-Meteorological");
        ReceiverThread thread2 = target.threadManager.get("JAlert-Cabinet");
        ReceiverThread thread3 = target.threadManager.get("JMA");

        // スレッド開始前確認
        assertThat(thread1.isAlive(), is(false));
        assertThat(thread2.isAlive(), is(false));
        assertThat(thread3.isAlive(), is(false));

        // スレッド開始
        target.start();

        // スレッド開始中確認
        assertThat(thread1.isAlive(), is(true));
        assertThat(thread2.isAlive(), is(true));
        assertThat(thread3.isAlive(), is(true));

        try {
            // 1秒スリープ
            Thread.sleep(1 * 1000);
        } catch (Exception ie) {
            // 特に処理なし
        }

        // スレッド停止
        target.stop();

        // スレッド停止中確認
        assertThat(thread1.isAlive(), is(false));
        assertThat(thread2.isAlive(), is(false));
        assertThat(thread3.isAlive(), is(false));

    }

    @Test(expected = ClassNotFoundException.class)
    public void DataAnalyzerインスタンス生成失敗() throws Exception {
        ReceiverConfig.getInstance().loadYml(
                "test/config/receiver_ReceiverThreadManagerTest2.yml");
        new ReceiverThreadManager();
    }

}
