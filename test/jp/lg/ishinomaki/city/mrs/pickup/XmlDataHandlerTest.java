package jp.lg.ishinomaki.city.mrs.pickup;

import java.lang.reflect.Field;

import jp.lg.ishinomaki.city.mrs.parser.JmaXmlDataParser;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class XmlDataHandlerTest {

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
    public void XMLデータ作成1() throws Exception {
        
        JmaXmlDataParser parser = new JmaXmlDataParser();
        
        Field projectIdField = parser.getClass().getDeclaredField("projectId");
        projectIdField.setAccessible(true);
        projectIdField.set(parser, "abcdefg");
        System.out.println("project-id: " + parser.getProjectId());
    }

}
