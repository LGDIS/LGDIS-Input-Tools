package jp.lg.ishinomaki.city.mrs.rest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Map;

import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;

import org.apache.http.HttpEntity;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IssuesPostControllerTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ParserConfig.getInstance().loadYml("config/parser.yml");
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
    public void インスタンス生成() {
        IssuesPostController target = new IssuesPostController();

        Map<String, Object> redmine = ParserConfig.getInstance().getRedmine();

        assertThat(target.protocol,
                is((String) redmine.get(ParserConfig.PROTOCOL)));
        assertThat(target.targetHost,
                is((String) redmine.get(ParserConfig.TARGET_HOST)));
        assertThat(target.targetPort,
                is((String) redmine.get(ParserConfig.TARGET_PORT)));
        assertThat(target.postApi,
                is((String) redmine.get(ParserConfig.ISSUES_POST_API)));
        assertThat(target.basicauthId,
                is((String) redmine.get(ParserConfig.BASICAUTH_ID)));
        assertThat(target.basicauthPassword,
                is((String) redmine.get(ParserConfig.BASICAUTH_PASSWORD)));
        assertThat(target.timeout,
                is((Integer) redmine.get(ParserConfig.TIMEOUT)));
        assertThat(target.apiKey,
                is((String) redmine.get(ParserConfig.API_KEY)));
        assertThat(target.retryCount,
                is((Integer) redmine.get(ParserConfig.RETRY_COUNT)));
        assertThat(target.contentType,
                is((String) redmine.get(ParserConfig.ISSUES_POST_CONTENT_TYPE)));
    }

    @Test
    public void 引数NullでHttpEntity作成() {
        IssuesPostController target = new IssuesPostController();
        HttpEntity actual = target.createHttpEntity(null);
        // 戻りがNullであることを確認
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void 引数Null以外でHttpEntity作成() {
        IssuesPostController target = new IssuesPostController();
        HttpEntity actual = target.createHttpEntity(new String());
        // 戻りがNullであることを確認
        assertThat(actual, is(notNullValue()));
    }

}
