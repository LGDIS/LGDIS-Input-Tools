package jp.lg.ishinomaki.city.mrs.receiver.jma;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JmaMessageTest {

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

    @Test(expected = IllegalArgumentException.class)
    public void パラメータチェックNull() {
        new JmaMessage(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void パラメータチェックレングス() {
        new JmaMessage("123456789".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void メッセージ長部分のデータが不正() {
        new JmaMessage("abcdefghijklmn".getBytes());
    }

    @Test
    public void メッセージ長部分のデータが実際のメッセージ長と等しい() {
        JmaMessage msg = new JmaMessage("00000010AB1234567890".getBytes());
        assertThat(msg.getUserDataLength(), is(10));
        assertThat(msg.getMessageLength(), is(10));
        assertThat(msg.isComplete(), is(true));
    }

    @Test
    public void メッセージ長部分のデータが実際のメッセージ長と等しくない() {
        JmaMessage msg = new JmaMessage("00000010AB123456789012345".getBytes());
        assertThat(msg.getUserDataLength(), is(15));
        assertThat(msg.getMessageLength(), is(10));
        assertThat(msg.isComplete(), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void メッセージタイプが制御レコードでレコード種別長が不足() {
        new JmaMessage("00000010EN1".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void メッセージタイプが制御レコードでレコード種別がCHKではない() {
        new JmaMessage("00000010EN1234567890".getBytes());
    }

    @Test
    public void メッセージタイプが制御レコードでレコード種別がCHK() {
        JmaMessage msg = new JmaMessage("00000010ENchk".getBytes());
        assertThat(msg.getMessageType(), is("EN"));
        assertThat(msg.getControlType(), is("chk"));
        assertThat(msg.isValidMessageType(), is(true));
    }

    @Test
    public void メッセージタイプがユーザデータでJMA仕様ではない() {
        JmaMessage msg = new JmaMessage("00000010ZZ1234567890".getBytes());
        assertThat(msg.getMessageType(), is("ZZ"));
        assertThat(msg.isValidMessageType(), is(false));
    }

    @Test
    public void メッセージタイプがユーザデータでJMA仕様1() {
        JmaMessage msg = new JmaMessage("00000010AN1234567890".getBytes());
        assertThat(msg.getMessageType(), is("AN"));
        assertThat(msg.isValidMessageType(), is(true));
        assertThat(msg.isCheckPoint(), is(false));
    }

    @Test
    public void メッセージタイプがユーザデータでJMA仕様2() {
        JmaMessage msg = new JmaMessage("00000010BI1234567890".getBytes());
        assertThat(msg.getMessageType(), is("BI"));
        assertThat(msg.isValidMessageType(), is(true));
        assertThat(msg.isCheckPoint(), is(false));
    }

    @Test
    public void メッセージタイプがユーザデータでJMA仕様3() {
        JmaMessage msg = new JmaMessage("00000010FX1234567890".getBytes());
        assertThat(msg.getMessageType(), is("FX"));
        assertThat(msg.isValidMessageType(), is(true));
        assertThat(msg.isCheckPoint(), is(false));
    }

    @Test
    public void メッセージタイプがユーザデータでJMA仕様4() {
        JmaMessage msg = new JmaMessage("00000010JL1234567890".getBytes());
        assertThat(msg.getMessageType(), is("JL"));
        assertThat(msg.isValidMessageType(), is(true));
        assertThat(msg.isCheckPoint(), is(false));
    }

    @Test
    public void チェックポイント応答電文() {
        JmaMessage msg = new JmaMessage("00000010BI1234567890".getBytes());
        byte[] ack = msg.generateCheckPointAck();
        assertThat(new String(ack), is("00000033ENACK00000010BI1234567890"));
    }

    @Test
    public void メッセージタイプがユーザデータでJMA仕様かつチェックポイントあり1() {
        JmaMessage msg = new JmaMessage("00000010aN1234567890".getBytes());
        assertThat(msg.getMessageType(), is("aN"));
        assertThat(msg.isValidMessageType(), is(true));
        assertThat(msg.isCheckPoint(), is(true));
    }

    @Test
    public void メッセージタイプがユーザデータでJMA仕様かつチェックポイントあり2() {
        JmaMessage msg = new JmaMessage("00000010bI1234567890".getBytes());
        assertThat(msg.getMessageType(), is("bI"));
        assertThat(msg.isValidMessageType(), is(true));
        assertThat(msg.isCheckPoint(), is(true));
    }

    @Test
    public void メッセージタイプがユーザデータでJMA仕様かつチェックポイントあり3() {
        JmaMessage msg = new JmaMessage("00000010fX1234567890".getBytes());
        assertThat(msg.getMessageType(), is("fX"));
        assertThat(msg.isValidMessageType(), is(true));
        assertThat(msg.isCheckPoint(), is(true));
    }

    @Test
    public void メッセージタイプがユーザデータでJMA仕様かつチェックポイントあり4() {
        JmaMessage msg = new JmaMessage("00000010jL1234567890".getBytes());
        assertThat(msg.getMessageType(), is("jL"));
        assertThat(msg.isValidMessageType(), is(true));
        assertThat(msg.isCheckPoint(), is(true));
    }

    @Test
    public void ヘルスチェック応答() {
        assertThat("00000003ENCHK",
                is(new String(JmaMessage.generateHelthcheckAck())));
    }
}
