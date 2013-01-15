package jp.lg.ishinomaki.city.mrs.queue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

/**
 * キュー機能にプッシュ接続するためのクライアントクラスです
 */
public class QueuePushClient {

    /**
     * コンストラクタ
     */
    public QueuePushClient() {
    }

    /**
     * キューにデータを登録します。
     * 
     * @throws IOException
     *             ソケット接続で例外発生時にスロー
     */
    public void push(byte[] data) throws IOException {

        // ドメインソケット用ファイル生成
        final File socketFile = new File(new File(
                System.getProperty(QueueConfig.DOMAIN_SOCKET_DIR_KEY)),
                QueueConfig.DOMAIN_SOCKET_FILE_FOR_PUSH);

        // ソケットインスタンス生成
        AFUNIXSocket sock = AFUNIXSocket.newInstance();
        sock.connect(new AFUNIXSocketAddress(socketFile));

        // データ送信
        OutputStream os = sock.getOutputStream();
        os.write(data);
        os.flush();

        // 後処理
        os.close();
        sock.close();
    }
}
