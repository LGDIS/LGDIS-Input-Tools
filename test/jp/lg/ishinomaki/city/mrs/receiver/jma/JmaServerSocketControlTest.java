package jp.lg.ishinomaki.city.mrs.receiver.jma;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JmaServerSocketControlTest {

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
    public void サーバソケット作成() {
        // テスト対象のインスタンス生成
        JmaServerSocketControl target = new JmaServerSocketControl("localhost",
                3600);
        target.setThreadName("threadName");
        // サーバソケット作成
        boolean actual = target.setup();
        assertThat(actual, is(true));

        // 後処理
        target.closeServerSocket();
    }

    @Test
    public void サーバソケット作成時に入出力エラー() {
        // テスト対象のインスタンス生成
        JmaServerSocketControl target = new JmaServerSocketControl("localhost",
                3600);
        target.setThreadName("threadName");
        // サーバソケット作成
        boolean actual = target.setup();
        assertThat(actual, is(true));

        // もう一度サーバソケット作成
        actual = target.setup();
        assertThat(actual, is(false));

        target.closeServerSocket();
    }

    @Test
    public void サーバソケットクローズ() {
        // テスト対象のインスタンス生成
        JmaServerSocketControl target = new JmaServerSocketControl("localhost",
                3600);
        target.setThreadName("threadName");
        // サーバソケット作成
        boolean actual = target.setup();
        assertThat(actual, is(true));

        // 例外が発生しないことを確認
        target.closeSocket();
        target.closeServerSocket();
    }

    @Test
    public void チェックポイントデータの結合と応答() throws Exception {
        // テスト対象クラスを生成
        JmaServerSocketControl target = new JmaServerSocketControl("localhost",
                3600);
        // テスト用JmaMessage
        JmaMessage msg = new JmaMessage(
                "00000030aN123456789012345678901234567890".getBytes());
        // 通常はソケットに出力するものをStringに格納するための設定
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        target.bufferedOutputStream = new BufferedOutputStream(dataStream);

        // テスト対象メソッド実行
        target.ackCheckpoint(msg);

        // チェックポイント応答電文内容をStringに格納
        String actual = byteStream.toString();
        assertThat(actual, is("00000033ENACK00000030aN12345678901234567890"));
    }

    @Test
    public void ヘルスチェック応答() throws Exception {
        // テスト対象クラスを生成
        JmaServerSocketControl target = new JmaServerSocketControl("localhost",
                3600);
        // 通常はソケットに出力するものをStringに格納するための設定
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        target.bufferedOutputStream = new BufferedOutputStream(dataStream);

        // テスト対象メソッド実行
        target.ackHelthCheck();

        // チェックポイント応答電文内容をStringに格納
        String actual = byteStream.toString();
        assertThat(actual, is("00000003ENCHK"));
    }

    @Test
    public void チェックポイントデータの結合() {
        // テスト対象クラスを生成
        JmaServerSocketControl target = new JmaServerSocketControl("localhost",
                3600);

        // チェックポイント登録用データ
        JmaMessage msg1 = new JmaMessage(
                "00000030aN123456789012345678901234567890".getBytes());
        JmaMessage msg2 = new JmaMessage(
                "00000030aNabcdefghijabcdefghijabcdefghij".getBytes());
        target.appendCheckpointManagedData(msg1);
        target.appendCheckpointManagedData(msg2);

        byte[] actual = target.mergeCheckpointManagedData();

        assertThat(
                new String(actual),
                is("123456789012345678901234567890abcdefghijabcdefghijabcdefghij"));
    }

}
