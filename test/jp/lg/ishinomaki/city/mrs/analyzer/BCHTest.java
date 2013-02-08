package jp.lg.ishinomaki.city.mrs.analyzer;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import jp.lg.ishinomaki.city.mrs.receiver.ReceiverConfig;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BCHTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ReceiverConfig.getInstance().loadYml("test/config/receiver.yml");
        // 0100010100000000000000001110010100001000010100000000000000000000001100000001001110110111100000100000000000000000000000000000000000000000000000000000000000000000
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
    public void バージョン番号1() {
        String bchStr = "000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        BCH bch = new BCH(bchStr.getBytes());
        assertThat(bch.getVersionNo(), is(1));
    }

    @Test
    public void バージョン番号2() {
        String bchStr = "0010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        BCH bch = new BCH(bchStr.getBytes());
        assertThat(bch.getVersionNo(), is(2));
    }

    @Test
    public void バージョン番号3() {
        String bchStr = "0011000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        BCH bch = new BCH(bchStr.getBytes());
        assertThat(bch.getVersionNo(), is(3));
    }

    @Test
    public void バージョン番号4() {
        String bchStr = "0100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        BCH bch = new BCH(bchStr.getBytes());
        assertThat(bch.getVersionNo(), is(4));
    }

    @Test
    public void バージョン番号5() {
        String bchStr = "0101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        BCH bch = new BCH(bchStr.getBytes());
        assertThat(bch.getVersionNo(), is(5));
    }

}
