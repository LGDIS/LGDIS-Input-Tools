package jp.lg.ishinomaki.city.mrs.queue;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class QueueClientTest {

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
    public void キューへメッセージ登録と取り出し() throws Exception {
        // サーバを起動する
        QueuePushServer pushServer = new QueuePushServer();
        pushServer.start();

        QueuePopServer popServer = new QueuePopServer();
        popServer.start();
        
        try {
            // 2秒スリープ
            Thread.sleep(2 * 1000);
        } catch (Exception ie) {
            // 特に処理なし
        }
        
        QueueClient target = new QueueClient();

        String data = "1BIXMLcontents";
        byte[] pushData = data.getBytes();

        // キューにメッセージ追加
        target.push(pushData);

        try {
            // 1秒スリープ
            Thread.sleep(1 * 1000);
        } catch (Exception ie) {
            // 特に処理なし
        }
        
        // メッセージがキューから正しく取れることを確認
        byte[] popData = target.pop();

        assertThat(popData, is(pushData));
    }

}
