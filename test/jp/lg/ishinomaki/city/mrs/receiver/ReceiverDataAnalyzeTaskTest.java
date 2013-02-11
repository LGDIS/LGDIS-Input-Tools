package jp.lg.ishinomaki.city.mrs.receiver;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import jp.lg.ishinomaki.city.mrs.analyzer.DataAnalyzer;
import jp.lg.ishinomaki.city.mrs.analyzer.JmaDataAnalyzer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class ReceiverDataAnalyzeTaskTest {

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

        byte[] data = "data".getBytes();
        String outputPath = "outputPath";
        DataAnalyzer analyzer = new JmaDataAnalyzer();
        String inputId = "inputId";
        int mode = 1;
        ReceiverDataAnalyzeTask target = new ReceiverDataAnalyzeTask(data,
                outputPath, analyzer, inputId, mode);

        assertThat(target.data, is(data));
        assertThat(target.outputPath, is(outputPath));
        assertThat(target.analyzer, is(sameInstance(analyzer)));
        assertThat(target.inputId, is(inputId));
        assertThat(target.mode, is(mode));
    }

    @Test
    public void キューデータ作成() {
        byte[] data = "data".getBytes();
        String outputPath = "outputPath";

        DataAnalyzer mock = Mockito.mock(JmaDataAnalyzer.class);
        when(mock.getDataType()).thenReturn("XML");
        String inputId = "AB";
        int mode = 1;
        ReceiverDataAnalyzeTask target = new ReceiverDataAnalyzeTask(data,
                outputPath, mock, inputId, mode);
        byte[] message = target.createMessage("contents".getBytes());

        // messageのデータ長は"contents"(8)にヘッダー分(6)を付与したものになる
        assertThat(message.length, is(14));
        assertThat(new String(message), is("1ABXMLcontents"));
    }

    @Test
    public void ファイル作成() {
        byte[] data = "data".getBytes();
        String outputPath = "test/temporaryFile";
        DataAnalyzer analyzer = new JmaDataAnalyzer();
        String inputId = "AB";
        int mode = 1;
        ReceiverDataAnalyzeTask target = new ReceiverDataAnalyzeTask(data,
                outputPath, analyzer, inputId, mode);
        target.createOutputFile("contents".getBytes());
    }

    @Test
    public void ファイル作成出力先指定なし() {
        byte[] data = "data".getBytes();
        String outputPath = null;
        DataAnalyzer analyzer = new JmaDataAnalyzer();
        String inputId = "AB";
        int mode = 1;
        ReceiverDataAnalyzeTask target = new ReceiverDataAnalyzeTask(data,
                outputPath, analyzer, inputId, mode);
        target.createOutputFile("contents".getBytes());
    }

    @Test
    public void runメソッド実行データなし() {
        byte[] data = null;
        String outputPath = null;
        DataAnalyzer analyzer = new JmaDataAnalyzer();
        String inputId = "AB";
        int mode = 1;
        ReceiverDataAnalyzeTask target = new ReceiverDataAnalyzeTask(data,
                outputPath, analyzer, inputId, mode);
        // Executorオブジェクトの生成
        Executor executor = Executors.newSingleThreadExecutor();
        // タスクの実行
        executor.execute(target);
    }

    @Test
    public void runメソッド実行データ解析中に例外発生() {
        byte[] data = "".getBytes();
        String outputPath = null;
        DataAnalyzer mock = Mockito.mock(JmaDataAnalyzer.class);
        doThrow(new UnsupportedOperationException()).when(mock).analyze(
                "".getBytes()); // analyzeメソッドで例外発生するよう設定

        String inputId = "AB";
        int mode = 1;
        ReceiverDataAnalyzeTask target = new ReceiverDataAnalyzeTask(data,
                outputPath, mock, inputId, mode);
        // Executorオブジェクトの生成
        Executor executor = Executors.newSingleThreadExecutor();
        // タスクの実行
        executor.execute(target);
    }

    @Test
    public void runメソッド実行contentsなし() {
        byte[] data = "data".getBytes();
        String outputPath = null;
        DataAnalyzer mock = Mockito.mock(JmaDataAnalyzer.class);
        when(mock.getContents()).thenReturn("".getBytes());
        String inputId = "AB";
        int mode = 1;
        ReceiverDataAnalyzeTask target = new ReceiverDataAnalyzeTask(data,
                outputPath, mock, inputId, mode);
        // Executorオブジェクトの生成
        Executor executor = Executors.newSingleThreadExecutor();
        // タスクの実行
        executor.execute(target);
    }

    @Test
    public void runメソッド実行contentsあり() {
        byte[] data = "data".getBytes();
        String outputPath = null;
        DataAnalyzer mock = Mockito.mock(JmaDataAnalyzer.class);
        when(mock.getContents()).thenReturn("contents".getBytes());
        when(mock.getDataType()).thenReturn("XML");
        String inputId = "AB";
        int mode = 1;
        ReceiverDataAnalyzeTask target = new ReceiverDataAnalyzeTask(data,
                outputPath, mock, inputId, mode);
        // Executorオブジェクトの生成
        Executor executor = Executors.newSingleThreadExecutor();
        // タスクの実行
        executor.execute(target);
    }
}
