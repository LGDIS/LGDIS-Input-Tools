package jp.lg.ishinomaki.city.mrs.receiver;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import jp.lg.ishinomaki.city.mrs.analyzer.DataAnalyzer;
import jp.lg.ishinomaki.city.mrs.analyzer.JmaDataAnalyzer;
import jp.lg.ishinomaki.city.mrs.receiver.jma.JmaServerSocketControl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReceiverThreadTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
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
    public void インスタンス生成() {

        String threadName = "testThread";
        JmaServerSocketControl socketControl = new JmaServerSocketControl(
                "ipaddress", 10000);
        String outputPath = "testOutputPath";
        Map<String, DataAnalyzer> analyzers = new HashMap<String, DataAnalyzer>();
        analyzers.put("testAnalyzer", new JmaDataAnalyzer());
        String inputId = "testInputId";
        int mode = 1;
        ReceiverThread target = new ReceiverThread(threadName, socketControl,
                outputPath, analyzers, inputId, mode);

        assertThat(target.threadName, is(threadName));
        assertThat(target.serverSocketControl, is(sameInstance(socketControl)));
        assertThat(target.outputPath, is(outputPath));
        assertThat(target.analyzers, is(sameInstance(analyzers)));
        assertThat(target.inputId, is(inputId));
        assertThat(target.mode, is(mode));
    }

    @Test
    public void スレッド開始後に停止() {
        String threadName = "testThread";
        JmaServerSocketControl socketControl = new JmaServerSocketControl(
                "localhost", 10000);
        String outputPath = "testOutputPath";
        Map<String, DataAnalyzer> analyzers = new HashMap<String, DataAnalyzer>();
        analyzers.put("testAnalyzer", new JmaDataAnalyzer());
        String inputId = "testInputId";
        int mode = 1;
        ReceiverThread target = new ReceiverThread(threadName, socketControl,
                outputPath, analyzers, inputId, mode);

        assertThat(target.isAlive(), is(false));

        // スレッド開始
        target.start();
        // スレッド開始後にスレッドが生存していることを確認
        assertThat(target.isAlive(), is(true));

        // 1秒スリープしてスレッド停止
        try {
            // 1秒スリープ
            Thread.sleep(1 * 1000);
        } catch (Exception ie) {
            // 特に処理なし
        }
        // スレッド停止
        target.done();

        // スレッド停止後にスレッドが生存していないことを確認
        assertThat(target.isAlive(), is(false));
    }

    @Test
    public void 受信データに対する解析クラスなし() {
        String threadName = "testThread";
        JmaServerSocketControl socketControl = new JmaServerSocketControl(
                "localhost", 10000);
        String outputPath = "testOutputPath";
        Map<String, DataAnalyzer> analyzers = new HashMap<String, DataAnalyzer>();
        analyzers.put("testAnalyzer", new JmaDataAnalyzer());
        String inputId = "testInputId";
        int mode = 1;
        ReceiverThread target = new ReceiverThread(threadName, socketControl,
                outputPath, analyzers, inputId, mode);

        // 例外が発生しないことを確認
        target.receiveData("testNNN", "testData".getBytes());
    }

    @Test
    public void 受信データに対する解析クラスあり() {
        String threadName = "testThread";
        JmaServerSocketControl socketControl = new JmaServerSocketControl(
                "localhost", 10000);
        String outputPath = "testOutputPath";
        Map<String, DataAnalyzer> analyzers = new HashMap<String, DataAnalyzer>();
        analyzers.put("testAnalyzer", new JmaDataAnalyzer());
        String inputId = "testInputId";
        int mode = 1;
        ReceiverThread target = new ReceiverThread(threadName, socketControl,
                outputPath, analyzers, inputId, mode);

        // 例外が発生しないことを確認
        target.receiveData("testAnalyzer", "testData".getBytes());
    }

}
