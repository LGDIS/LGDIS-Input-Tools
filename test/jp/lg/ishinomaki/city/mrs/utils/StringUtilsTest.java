package jp.lg.ishinomaki.city.mrs.utils;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StringUtilsTest {

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
    public void 文字列のブランクチェックNullの場合() {
        boolean actual = StringUtils.isBlank(null);
        assertThat(actual, is(true));
    }

    @Test
    public void 文字列のブランクチェック空文字の場合() {
        boolean actual = StringUtils.isBlank("");
        assertThat(actual, is(true));
    }

    @Test
    public void 文字列のブランクチェック文字ありの場合() {
        boolean actual = StringUtils.isBlank("abc");
        assertThat(actual, is(false));
    }

    @Test
    public void 震度の比較1() {
        boolean actual = StringUtils.compareSeismicIntensity("3", "4");
        assertThat(actual, is(false));
    }

    @Test
    public void 震度の比較2() {
        boolean actual = StringUtils.compareSeismicIntensity("4", "3");
        assertThat(actual, is(true));
    }

    @Test
    public void 震度の比較3() {
        boolean actual = StringUtils.compareSeismicIntensity("5+", "5-");
        assertThat(actual, is(true));
    }

    @Test
    public void 震度の比較4() {
        boolean actual = StringUtils.compareSeismicIntensity("5+", "5");
        assertThat(actual, is(true));
    }

    @Test
    public void 震度の比較5() {
        boolean actual = StringUtils.compareSeismicIntensity("5", "5-");
        assertThat(actual, is(true));
    }

    @Test
    public void 震度の比較6() {
        boolean actual = StringUtils.compareSeismicIntensity("6", "1");
        assertThat(actual, is(true));
    }

    @Test
    public void 震度の変換1() {
        double actual = StringUtils.convertSeismicIntensityToDouble("1");
        assertThat(actual, is((double) 1));
    }

    @Test
    public void 震度の変換2() {
        double actual = StringUtils.convertSeismicIntensityToDouble("5-");
        assertThat(actual, is(4.5));
    }

    @Test
    public void 震度の変換3() {
        double actual = StringUtils.convertSeismicIntensityToDouble("5+");
        assertThat(actual, is(5.5));
    }

    @Test
    public void 度のポイント情報をRest送信用文字列に変換1() {
        String actual = StringUtils.convertPoint("+21.2+135.5/");
        assertThat(actual, is("(+135.5,+21.2)"));
    }

    @Test
    public void 度のポイント情報をRest送信用文字列に変換2() {
        String actual = StringUtils.convertPoint("+2000+13250/");
        assertThat(actual, is("(+132.83,+20.0)"));
    }

    @Test
    public void 度のポイント情報をRest送信用文字列に変換3() {
        String actual = StringUtils.convertPoint("-21.2-135.5/");
        assertThat(actual, is("(-135.5,-21.2)"));
    }

    @Test
    public void 度のポイント情報をRest送信用文字列に変換4() {
        String actual = StringUtils.convertPoint("-2000-13250/");
        assertThat(actual, is("(-132.83,-20.0)"));
    }

    @Test
    public void 度のポイント情報をRest送信用文字列に変換5() {
        String actual = StringUtils.convertPoint("-21.2-135.5-7000/");
        assertThat(actual, is("(-135.5,-21.2)"));
    }

    @Test
    public void 度のポイント情報をRest送信用文字列に変換6() {
        String actual = StringUtils.convertPoint("-2000-13250-7000/");
        assertThat(actual, is("(-132.83,-20.0)"));
    }

    @Test
    public void 度分かチェック1() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("100");
        assertThat(actual, is(true));
    }

    @Test
    public void 度分かチェック2() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("100.0");
        assertThat(actual, is(true));
    }

    @Test
    public void 度分かチェック3() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("99");
        assertThat(actual, is(false));
    }

    @Test
    public void 度分かチェック4() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("99.0");
        assertThat(actual, is(false));
    }

    @Test
    public void 度分かチェック5() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("99.99");
        assertThat(actual, is(false));
    }

    @Test
    public void 度分かチェック6() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("9000");
        assertThat(actual, is(true));
    }

    @Test
    public void 度分かチェック7() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("0");
        assertThat(actual, is(false));
    }

    @Test
    public void 度分かチェック8() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("0.00");
        assertThat(actual, is(false));
    }

    @Test
    public void 度分かチェック9() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("100.00");
        assertThat(actual, is(true));
    }

    @Test
    public void 度分かチェック10() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("+100");
        assertThat(actual, is(true));
    }

    @Test
    public void 度分かチェック11() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("-100.0");
        assertThat(actual, is(true));
    }

    @Test
    public void 度分かチェック12() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("+99");
        assertThat(actual, is(false));
    }

    @Test
    public void 度分かチェック13() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("-99.0");
        assertThat(actual, is(false));
    }

    @Test
    public void 度分かチェック14() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("+99.99");
        assertThat(actual, is(false));
    }

    @Test
    public void 度分かチェック15() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("-9000");
        assertThat(actual, is(true));
    }

    @Test
    public void 度分かチェック16() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("+0");
        assertThat(actual, is(false));
    }

    @Test
    public void 度分かチェック17() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("-0.00");
        assertThat(actual, is(false));
    }

    @Test
    public void 度分かチェック18() {
        boolean actual = StringUtils.isDOFUNBYOUbyLatitude("+100.00");
        assertThat(actual, is(true));
    }

    @Test
    public void 度分を度に変換1() {
        String actual = StringUtils.convertDOFUNBYOUtoDO("4232.63");
        assertThat(actual, is("42.55"));
    }

    @Test
    public void 度分を度に変換2() {
        String actual = StringUtils.convertDOFUNBYOUtoDO("14050.35");
        assertThat(actual, is("140.84"));
    }

    @Test
    public void 度分を度に変換3() {
        String actual = StringUtils.convertDOFUNBYOUtoDO("2000");
        assertThat(actual, is("20.0"));
    }

    @Test
    public void 度分を度に変換4() {
        String actual = StringUtils.convertDOFUNBYOUtoDO("13250");
        assertThat(actual, is("132.83"));
    }

    @Test
    public void 度分を度に変換5() {
        String actual = StringUtils.convertDOFUNBYOUtoDO("4232.6");
        assertThat(actual, is("42.53"));
    }

    @Test
    public void 度分を度に変換6() {
        String actual = StringUtils.convertDOFUNBYOUtoDO("4232.6399");
        assertThat(actual, is("44.31"));
    }

    @Test
    public void 度分を度に変換7() {
        String actual = StringUtils.convertDOFUNBYOUtoDO("100");
        assertThat(actual, is("1.0"));
    }

    @Test
    public void 度分を度に変換8() {
        String actual = StringUtils.convertDOFUNBYOUtoDO("+4232.63");
        assertThat(actual, is("+42.55"));
    }

    @Test
    public void 度分を度に変換9() {
        String actual = StringUtils.convertDOFUNBYOUtoDO("-14050.35");
        assertThat(actual, is("-140.84"));
    }

    @Test
    public void 度分を度に変換10() {
        String actual = StringUtils.convertDOFUNBYOUtoDO("+2000");
        assertThat(actual, is("+20.0"));
    }

    @Test
    public void 度分を度に変換11() {
        String actual = StringUtils.convertDOFUNBYOUtoDO("-13250");
        assertThat(actual, is("-132.83"));
    }

    @Test
    public void 度分を度に変換12() {
        String actual = StringUtils.convertDOFUNBYOUtoDO("+4232.6");
        assertThat(actual, is("+42.53"));
    }

    @Test
    public void 度分を度に変換13() {
        String actual = StringUtils.convertDOFUNBYOUtoDO("-4232.6399");
        assertThat(actual, is("-44.31"));
    }

    @Test
    public void 度分を度に変換14() {
        String actual = StringUtils.convertDOFUNBYOUtoDO("+100");
        assertThat(actual, is("+1.0"));
    }

}
