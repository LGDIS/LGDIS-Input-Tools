package jp.lg.ishinomaki.city.mrs.utils;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BCHChecksumHelperTest {

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
    public void チェックサムOK() {
        String bch = "0100010100000000000000001110010100001000010100000000000000000000001100000001001110110111100000100000000000000000000000000000000000000000000000000000000000000000";
        String checksum = "1000000110110111";
        boolean actual = BCHChecksumHelper.check(bch, checksum);
        assertThat(actual, is(true));
    }

    @Test
    public void チェックサムNG() {
        String bch = "0100010100000000000000001110010100001000010100000000000000000000001100000001001110110111100000100000000000000000000000000000000000000000000000000000000000000000";
        String checksum = "1000000110110110";
        boolean actual = BCHChecksumHelper.check(bch, checksum);
        assertThat(actual, is(false));
    }

}
