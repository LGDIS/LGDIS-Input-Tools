package jp.lg.ishinomaki.city.mrs.utils;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileUtilsTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

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
    public void パラメータ不正1() {
        boolean actual = FileUtils.saveContentsAsFile(null, "aaa", "bbb");
        assertThat(actual, is(false));
    }

    @Test
    public void パラメータ不正2() {
        boolean actual = FileUtils.saveContentsAsFile("".getBytes(), "aaa",
                "bbb");
        assertThat(actual, is(false));
    }

    @Test
    public void パラメータ不正3() {
        boolean actual = FileUtils.saveContentsAsFile("abc".getBytes(), null,
                "bbb");
        assertThat(actual, is(false));
    }

    @Test
    public void パラメータ不正4() {
        boolean actual = FileUtils.saveContentsAsFile("abc".getBytes(), "",
                "bbb");
        assertThat(actual, is(false));
    }

    @Test
    public void パラメータ不正5() {
        boolean actual = FileUtils.saveContentsAsFile("abc".getBytes(), "aaa",
                null);
        assertThat(actual, is(false));
    }

    @Test
    public void パラメータ不正6() {
        boolean actual = FileUtils.saveContentsAsFile("abc".getBytes(), "aaa",
                "");
        assertThat(actual, is(false));
    }

    @Test
    public void ファイル作成成功() {
        File folder = tmpFolder.getRoot();
        String outputPath = folder.getAbsolutePath();

        boolean actual = FileUtils.saveContentsAsFile("abc".getBytes(),
                outputPath, "bbb");
        assertThat(actual, is(true));

        String[] actualFiles = folder.list();
        assertThat(actualFiles.length, is(1));
        assertThat(actualFiles[0], is("bbb"));
    }

    @Test
    public void ファイル名生成() {
        String actual = FileUtils.genFileName("test");
        // ファイル名に日付が入るので一致チェックできないため文字列長でチェック
        assertThat(actual.length(), is(27));
    }

}
