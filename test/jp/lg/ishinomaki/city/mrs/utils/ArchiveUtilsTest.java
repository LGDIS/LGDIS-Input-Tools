package jp.lg.ishinomaki.city.mrs.utils;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ArchiveUtilsTest {

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
    public void gzipの解凍() throws Exception {
        String archiveFile = "test/archive/gzip.gz";
        byte raw[] = new byte[(int) ((new File(archiveFile)).length())];
        FileInputStream fis = new FileInputStream(archiveFile);
        fis.read(raw);
        fis.close();
        byte[] ungzip = ArchiveUtils.ungzip(raw);

        String actual = new String(ungzip);
        assertThat(actual, is("gzip"));
    }

    @Test
    public void zipの解凍() throws Exception {
        String archiveFile = "test/archive/zip.zip";
        byte raw[] = new byte[(int) ((new File(archiveFile)).length())];
        FileInputStream fis = new FileInputStream(archiveFile);
        fis.read(raw);
        fis.close();
        byte[] unzip = ArchiveUtils.unzip(raw);

        String actual = new String(unzip);
        assertThat(actual, is("zip"));
    }
    
    @Test
    public void tarの解凍() throws Exception {
        String archiveFile = "test/archive/tar.tar";
        byte raw[] = new byte[(int) ((new File(archiveFile)).length())];
        FileInputStream fis = new FileInputStream(archiveFile);
        fis.read(raw);
        fis.close();
        List<Map<String, Object>> fileInfos = ArchiveUtils.untar(raw);
        Map<String, Object> fileInfo1 = fileInfos.get(0);
        Map<String, Object> fileInfo2 = fileInfos.get(1);
        assertThat((String)fileInfo1.get("name"), is("tar1.txt"));
        assertThat((String)fileInfo2.get("name"), is("tar2.txt"));
        
        byte[] contents1 = (byte[])fileInfo1.get("contents");
        byte[] contents2 = (byte[])fileInfo2.get("contents");
        
        assertThat(new String(contents1), is("tar1\n"));
        assertThat(new String(contents2), is("tar2\n"));
    }

}
