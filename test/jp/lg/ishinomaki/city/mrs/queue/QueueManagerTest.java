package jp.lg.ishinomaki.city.mrs.queue;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class QueueManagerTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        QueueConfig.getInstance().loadYml("config/queue.yml");
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
    public void キューにデータを登録して取り出し() throws Exception {
        // テスト対象クラス生成
        QueueManager target = QueueManager.getInstance();

        byte[] data = "testdata".getBytes();
        target.push(data);
        assertThat(target.pop(), is(data));
    }

}
