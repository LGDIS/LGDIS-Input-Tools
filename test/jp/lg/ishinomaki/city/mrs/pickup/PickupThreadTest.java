package jp.lg.ishinomaki.city.mrs.pickup;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PickupThreadTest {

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
    public void 通常モードデータ() {
        // テストターゲットクラス
        PickupThread target = new PickupThread();

        // ダミーデータ(モード:0)
        byte[] data = new byte[8];
        System.arraycopy("0".getBytes(), 0, data, 0, 1);
        System.arraycopy("JMA".getBytes(), 0, data, 1, 3);
        System.arraycopy("XML".getBytes(), 0, data, 4, 3);
        // ヘッダ以降にcontentsのバイト配列を設定
        System.arraycopy("1".getBytes(), 0, data, 7, 1);

        int actual = target.getMode(data);
        assertThat(actual, is(0));
    }

    @Test
    public void 訓練モードデータ() {
        // テストターゲットクラス
        PickupThread target = new PickupThread();

        // ダミーデータ(モード:1)
        byte[] data = new byte[8];
        System.arraycopy("1".getBytes(), 0, data, 0, 1);
        System.arraycopy("JMA".getBytes(), 0, data, 1, 3);
        System.arraycopy("XML".getBytes(), 0, data, 4, 3);
        // ヘッダ以降にcontentsのバイト配列を設定
        System.arraycopy("1".getBytes(), 0, data, 7, 1);

        int actual = target.getMode(data);
        assertThat(actual, is(1));
    }

    @Test
    public void 試験モードデータ() {
        // テストターゲットクラス
        PickupThread target = new PickupThread();

        // ダミーデータ(モード:2)
        byte[] data = new byte[8];
        System.arraycopy("2".getBytes(), 0, data, 0, 1);
        System.arraycopy("JMA".getBytes(), 0, data, 1, 3);
        System.arraycopy("XML".getBytes(), 0, data, 4, 3);
        // ヘッダ以降にcontentsのバイト配列を設定
        System.arraycopy("1".getBytes(), 0, data, 7, 1);

        int actual = target.getMode(data);
        assertThat(actual, is(2));
    }

    @Test
    public void 入力元識別子() {
        // テストターゲットクラス
        PickupThread target = new PickupThread();

        // ダミーデータ(モード:0)
        byte[] data = new byte[8];
        System.arraycopy("0".getBytes(), 0, data, 0, 1);
        System.arraycopy("JMA".getBytes(), 0, data, 1, 3);
        System.arraycopy("XML".getBytes(), 0, data, 4, 3);
        // ヘッダ以降にcontentsのバイト配列を設定
        System.arraycopy("1".getBytes(), 0, data, 7, 1);

        String actual = target.getInputId(data);
        assertThat(actual, is("JMA"));
    }

    @Test
    public void データ種別() {
        // テストターゲットクラス
        PickupThread target = new PickupThread();

        // ダミーデータ(モード:0)
        byte[] data = new byte[8];
        System.arraycopy("0".getBytes(), 0, data, 0, 1);
        System.arraycopy("JMA".getBytes(), 0, data, 1, 3);
        System.arraycopy("XML".getBytes(), 0, data, 4, 3);
        // ヘッダ以降にcontentsのバイト配列を設定
        System.arraycopy("1".getBytes(), 0, data, 7, 1);

        String actual = target.getDataType(data);
        assertThat(actual, is("XML"));
    }

    @Test
    public void 本文内容() {
        // テストターゲットクラス
        PickupThread target = new PickupThread();

        String contents = "あいうえお";
        byte[] bcontents = contents.getBytes();
        byte[] data = new byte[7 + bcontents.length];
        System.arraycopy("0".getBytes(), 0, data, 0, 1);
        System.arraycopy("JMA".getBytes(), 0, data, 1, 3);
        System.arraycopy("XML".getBytes(), 0, data, 4, 3);
        // ヘッダ以降にcontentsのバイト配列を設定
        System.arraycopy(bcontents, 0, data, 7, bcontents.length);

        String actual = new String(target.getContents(data));
        assertThat(actual, is(contents));
    }

}
