package jp.lg.ishinomaki.city.mrs.pickup;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;
import jp.lg.ishinomaki.city.mrs.rest.IssuesPostController;
import jp.lg.ishinomaki.city.mrs.rest.UploadsPostController;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class TarDataHandlerTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ParserConfig.getInstance().loadYml("test/config/parser.yml");
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
    public void uploadsAPIの戻りデータがNull() throws Exception {
        // 例外が発生しないことを確認
        TarDataHandler target = new TarDataHandler(0, "JMA");

        UploadsPostController mock = Mockito.mock(UploadsPostController.class);
        target.uploadsController = mock;
        when(mock.post(any())).thenReturn(null);

        // テスト用tarファイルの読み込み
        String archiveFile = "test/archive/tar.tar";
        byte raw[] = new byte[(int) ((new File(archiveFile)).length())];
        FileInputStream fis = new FileInputStream(archiveFile);
        fis.read(raw);
        fis.close();

        target.handle(raw);
    }

    @Test
    public void uploadsAPIの戻りデータがXML形式ではない() throws Exception {
        // 例外が発生しないことを確認
        TarDataHandler target = new TarDataHandler(0, "JMA");

        UploadsPostController mock = Mockito.mock(UploadsPostController.class);
        target.uploadsController = mock;
        when(mock.post(any())).thenReturn("abcdefg");

        // テスト用tarファイルの読み込み
        String archiveFile = "test/archive/tar.tar";
        byte raw[] = new byte[(int) ((new File(archiveFile)).length())];
        FileInputStream fis = new FileInputStream(archiveFile);
        fis.read(raw);
        fis.close();

        target.handle(raw);
    }

    @Test
    public void uploadsAPIの戻りデータにtokenがない() throws Exception {
        // 例外が発生しないことを確認
        TarDataHandler target = new TarDataHandler(0, "JMA");

        UploadsPostController mock = Mockito.mock(UploadsPostController.class);
        target.uploadsController = mock;
        when(mock.post(any())).thenReturn("<upload><token></token></upload>");

        // テスト用tarファイルの読み込み
        String archiveFile = "test/archive/tar.tar";
        byte raw[] = new byte[(int) ((new File(archiveFile)).length())];
        FileInputStream fis = new FileInputStream(archiveFile);
        fis.read(raw);
        fis.close();

        target.handle(raw);
    }

    @Test
    public void uploadsAPIの戻りデータにtokenあり() throws Exception {
        // 例外が発生しないことを確認
        TarDataHandler target = new TarDataHandler(0, "JMA");

        UploadsPostController mock = Mockito.mock(UploadsPostController.class);
        IssuesPostController mock2 = Mockito.mock(IssuesPostController.class);
        target.uploadsController = mock;
        target.issuesController = mock2;
        when(mock.post(any())).thenReturn(
                "<upload><token>token</token></upload>");
        when(mock2.post(any())).thenReturn("");

        // テスト用tarファイルの読み込み
        String archiveFile = "test/archive/tar.tar";
        byte raw[] = new byte[(int) ((new File(archiveFile)).length())];
        FileInputStream fis = new FileInputStream(archiveFile);
        fis.read(raw);
        fis.close();

        target.handle(raw);
    }

    @Test
    public void uploadsAPIの戻りデータが空文字() throws Exception {
        // 例外が発生しないことを確認
        TarDataHandler target = new TarDataHandler(0, "JMA");

        UploadsPostController mock = Mockito.mock(UploadsPostController.class);
        target.uploadsController = mock;
        when(mock.post(any())).thenReturn("");
        when(mock.post(any())).thenReturn("");

        // テスト用tarファイルの読み込み
        String archiveFile = "test/archive/tar.tar";
        byte raw[] = new byte[(int) ((new File(archiveFile)).length())];
        FileInputStream fis = new FileInputStream(archiveFile);
        fis.read(raw);
        fis.close();

        target.handle(raw);
    }

    @Test
    public void 通常モード用xml作成() throws Exception {
        // xml作成
        TarDataHandler target = new TarDataHandler(0, "JMA");
        List<Map<String, String>> uploadedFiles = new ArrayList<Map<String, String>>();
        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("token", "token1");
        map1.put("filename", "text1.txt");
        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("token", "token2");
        map2.put("filename", "text2.txt");
        uploadedFiles.add(map1);
        uploadedFiles.add(map2);
        String xml = target.createIssuesXmlAsString(uploadedFiles);

        // XPathを使用してRedmineから返却されたトークン情報を取得
        Document doc = DocumentHelper.parseText(xml);
        assertThat(doc.valueOf("/issue/uploads/upload[1]/token/text()"),
                is("token1"));
        assertThat(doc.valueOf("/issue/uploads/upload[2]/token/text()"),
                is("token2"));

        Map<String, String> map = ParserConfig.getInstance()
                .getTarAttachmentStatics();
        // subject
        String subject = map.get(ParserConfig.SUBJECT);
        assertThat(doc.valueOf("/issue/subject/text()"), is(subject));
        // tracier_id
        String trackerId = map.get(ParserConfig.TRACKER_ID);
        assertThat(doc.valueOf("/issue/tracker_id/text()"), is(trackerId));
        // project_id
        String projectId = map.get(ParserConfig.PROJECT_ID);
        assertThat(doc.valueOf("/issue/project_id/text()"), is(projectId));
        // filename
        assertThat(doc.valueOf("/issue/uploads/upload[1]/filename/text()"),
                is("text1.txt"));
        assertThat(doc.valueOf("/issue/uploads/upload[2]/filename/text()"),
                is("text2.txt"));
        // description
        String description = map.get(ParserConfig.DESCRIPTION);
        assertThat(doc.valueOf("/issue/uploads/upload[1]/description/text()"),
                is(description));
        assertThat(doc.valueOf("/issue/uploads/upload[2]/description/text()"),
                is(description));
        // content_type
        String contentType = map.get(ParserConfig.CONTENT_TYPE);
        assertThat(doc.valueOf("/issue/uploads/upload[1]/content_type/text()"),
                is(contentType));
        assertThat(doc.valueOf("/issue/uploads/upload[2]/content_type/text()"),
                is(contentType));
    }

    @Test
    public void 訓練モード用xml作成() throws Exception {
        // xml作成
        TarDataHandler target = new TarDataHandler(1, "JMA");
        List<Map<String, String>> uploadedFiles = new ArrayList<Map<String, String>>();
        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("token", "token1");
        map1.put("filename", "text1.txt");
        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("token", "token2");
        map2.put("filename", "text2.txt");
        uploadedFiles.add(map1);
        uploadedFiles.add(map2);
        String xml = target.createIssuesXmlAsString(uploadedFiles);

        // XPathを使用してRedmineから返却されたトークン情報を取得
        Document doc = DocumentHelper.parseText(xml);
        assertThat(doc.valueOf("/issue/uploads/upload[1]/token/text()"),
                is("token1"));
        assertThat(doc.valueOf("/issue/uploads/upload[2]/token/text()"),
                is("token2"));

        Map<String, String> map = ParserConfig.getInstance()
                .getTarAttachmentStatics();
        // subject
        String subject = map.get(ParserConfig.SUBJECT);
        assertThat(doc.valueOf("/issue/subject/text()"), is(subject));
        // tracier_id
        String trackerId = map.get(ParserConfig.TRACKER_ID);
        assertThat(doc.valueOf("/issue/tracker_id/text()"), is(trackerId));
        // project_id
        String projectId = ParserConfig.getInstance().getTrainingProjectId();
        assertThat(doc.valueOf("/issue/project_id/text()"), is(projectId));
        // filename
        assertThat(doc.valueOf("/issue/uploads/upload[1]/filename/text()"),
                is("text1.txt"));
        assertThat(doc.valueOf("/issue/uploads/upload[2]/filename/text()"),
                is("text2.txt"));
        // description
        String description = map.get(ParserConfig.DESCRIPTION);
        assertThat(doc.valueOf("/issue/uploads/upload[1]/description/text()"),
                is(description));
        assertThat(doc.valueOf("/issue/uploads/upload[2]/description/text()"),
                is(description));
        // content_type
        String contentType = map.get(ParserConfig.CONTENT_TYPE);
        assertThat(doc.valueOf("/issue/uploads/upload[1]/content_type/text()"),
                is(contentType));
        assertThat(doc.valueOf("/issue/uploads/upload[2]/content_type/text()"),
                is(contentType));
    }

    @Test
    public void 試験モード用xml作成() throws Exception {
        // xml作成
        TarDataHandler target = new TarDataHandler(2, "JMA");
        List<Map<String, String>> uploadedFiles = new ArrayList<Map<String, String>>();
        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("token", "token1");
        map1.put("filename", "text1.txt");
        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("token", "token2");
        map2.put("filename", "text2.txt");
        uploadedFiles.add(map1);
        uploadedFiles.add(map2);
        String xml = target.createIssuesXmlAsString(uploadedFiles);

        // XPathを使用してRedmineから返却されたトークン情報を取得
        Document doc = DocumentHelper.parseText(xml);
        assertThat(doc.valueOf("/issue/uploads/upload[1]/token/text()"),
                is("token1"));
        assertThat(doc.valueOf("/issue/uploads/upload[2]/token/text()"),
                is("token2"));

        Map<String, String> map = ParserConfig.getInstance()
                .getTarAttachmentStatics();
        // subject
        String subject = map.get(ParserConfig.SUBJECT);
        assertThat(doc.valueOf("/issue/subject/text()"), is(subject));
        // tracier_id
        String trackerId = map.get(ParserConfig.TRACKER_ID);
        assertThat(doc.valueOf("/issue/tracker_id/text()"), is(trackerId));
        // project_id
        String projectId = ParserConfig.getInstance().getTestProjectId();
        assertThat(doc.valueOf("/issue/project_id/text()"), is(projectId));
        // filename
        assertThat(doc.valueOf("/issue/uploads/upload[1]/filename/text()"),
                is("text1.txt"));
        assertThat(doc.valueOf("/issue/uploads/upload[2]/filename/text()"),
                is("text2.txt"));
        // description
        String description = map.get(ParserConfig.DESCRIPTION);
        assertThat(doc.valueOf("/issue/uploads/upload[1]/description/text()"),
                is(description));
        assertThat(doc.valueOf("/issue/uploads/upload[2]/description/text()"),
                is(description));
        // content_type
        String contentType = map.get(ParserConfig.CONTENT_TYPE);
        assertThat(doc.valueOf("/issue/uploads/upload[1]/content_type/text()"),
                is(contentType));
        assertThat(doc.valueOf("/issue/uploads/upload[2]/content_type/text()"),
                is(contentType));
    }
}
