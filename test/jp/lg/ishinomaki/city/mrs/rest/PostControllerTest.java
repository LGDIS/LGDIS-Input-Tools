package jp.lg.ishinomaki.city.mrs.rest;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PostControllerTest {

    // テスト対象クラスが抽象クラスのため実装クラスを定義
    private class ConcreatePostController extends PostController {

        @Override
        HttpEntity createHttpEntity(Object data) {
            HttpEntity httpEntity = null;
            try {
                httpEntity = new StringEntity((String)data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return httpEntity;
        }
        
        
    }
    
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
    public void test() {
        fail("Not yet implemented");
    }

}
