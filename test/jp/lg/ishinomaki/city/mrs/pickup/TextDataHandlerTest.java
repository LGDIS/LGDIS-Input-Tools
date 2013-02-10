package jp.lg.ishinomaki.city.mrs.pickup;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

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

public class TextDataHandlerTest {

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
    public void uploadsAPIの戻りデータがNull() {
        // 例外が発生しないことを確認
        TextDataHandler target = new TextDataHandler();

        UploadsPostController mock = Mockito.mock(UploadsPostController.class);
        target.uploadsController = mock;
        when(mock.post(any())).thenReturn(null);
        target.handle("test".getBytes());
    }

    @Test
    public void uploadsAPIの戻りデータがXML形式ではない() {
        // 例外が発生しないことを確認
        TextDataHandler target = new TextDataHandler();

        UploadsPostController mock = Mockito.mock(UploadsPostController.class);
        target.uploadsController = mock;
        when(mock.post(any())).thenReturn("abcdefg");
        target.handle("test".getBytes());
    }

    @Test
    public void uploadsAPIの戻りデータにtokenがない() {
        // 例外が発生しないことを確認
        TextDataHandler target = new TextDataHandler();

        UploadsPostController mock = Mockito.mock(UploadsPostController.class);
        target.uploadsController = mock;
        when(mock.post(any())).thenReturn("<upload><token></token></upload>");
        target.handle("test".getBytes());
    }

    @Test
    public void uploadsAPIの戻りデータにtokenあり() {
        // 例外が発生しないことを確認
        TextDataHandler target = new TextDataHandler();

        UploadsPostController mock = Mockito.mock(UploadsPostController.class);
        IssuesPostController mock2 = Mockito.mock(IssuesPostController.class);
        target.uploadsController = mock;
        target.issuesController = mock2;
        when(mock.post(any())).thenReturn(
                "<upload><token>token</token></upload>");
        when(mock2.post(any())).thenReturn("");
        target.handle("test".getBytes());
    }

    @Test
    public void uploadsAPIの戻りデータが空文字() {
        // 例外が発生しないことを確認
        TextDataHandler target = new TextDataHandler();

        UploadsPostController mock = Mockito.mock(UploadsPostController.class);
        target.uploadsController = mock;
        when(mock.post(any())).thenReturn("");
        target.handle("test".getBytes());
    }

    @Test
    public void 通常モード用xml作成() throws Exception {
        // xml作成
        TextDataHandler target = new TextDataHandler();
        String xml = target.createIssuesXmlAsString("token");

        // XPathを使用してRedmineから返却されたトークン情報を取得
        Document doc = DocumentHelper.parseText(xml);
        assertThat(doc.valueOf("/issue/uploads/upload/token/text()"),
                is("token"));

        Map<String, String> map = ParserConfig.getInstance()
                .getTextAttachmentStatics();
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
        String filename = map.get(ParserConfig.FILENAME);
        assertThat(doc.valueOf("/issue/uploads/upload/filename/text()"),
                is(filename));
        // description
        String description = map.get(ParserConfig.DESCRIPTION);
        assertThat(doc.valueOf("/issue/uploads/upload/description/text()"),
                is(description));
        // content_type
        String contentType = map.get(ParserConfig.CONTENT_TYPE);
        assertThat(doc.valueOf("/issue/uploads/upload/content_type/text()"),
                is(contentType));
    }

    @Test
    public void 訓練モード用xml作成() throws Exception {
        // xml作成
        TextDataHandler target = new TextDataHandler(1);
        String xml = target.createIssuesXmlAsString("token");

        // XPathを使用してRedmineから返却されたトークン情報を取得
        Document doc = DocumentHelper.parseText(xml);
        assertThat(doc.valueOf("/issue/uploads/upload/token/text()"),
                is("token"));

        Map<String, String> map = ParserConfig.getInstance()
                .getTextAttachmentStatics();
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
        String filename = map.get(ParserConfig.FILENAME);
        assertThat(doc.valueOf("/issue/uploads/upload/filename/text()"),
                is(filename));
        // description
        String description = map.get(ParserConfig.DESCRIPTION);
        assertThat(doc.valueOf("/issue/uploads/upload/description/text()"),
                is(description));
        // content_type
        String contentType = map.get(ParserConfig.CONTENT_TYPE);
        assertThat(doc.valueOf("/issue/uploads/upload/content_type/text()"),
                is(contentType));
    }

    @Test
    public void 試験モード用xml作成() throws Exception {
        // xml作成
        TextDataHandler target = new TextDataHandler(2);
        String xml = target.createIssuesXmlAsString("token");

        // XPathを使用してRedmineから返却されたトークン情報を取得
        Document doc = DocumentHelper.parseText(xml);
        assertThat(doc.valueOf("/issue/uploads/upload/token/text()"),
                is("token"));

        Map<String, String> map = ParserConfig.getInstance()
                .getTextAttachmentStatics();
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
        String filename = map.get(ParserConfig.FILENAME);
        assertThat(doc.valueOf("/issue/uploads/upload/filename/text()"),
                is(filename));
        // description
        String description = map.get(ParserConfig.DESCRIPTION);
        assertThat(doc.valueOf("/issue/uploads/upload/description/text()"),
                is(description));
        // content_type
        String contentType = map.get(ParserConfig.CONTENT_TYPE);
        assertThat(doc.valueOf("/issue/uploads/upload/content_type/text()"),
                is(contentType));
    }

}
