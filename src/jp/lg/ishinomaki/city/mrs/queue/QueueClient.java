//
//  QueueClient.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.queue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

/**
 * キュー機能に接続するためのクライアントクラスです
 */
public class QueueClient {

    Logger log = Logger.getLogger(QueueClient.class.getSimpleName());

    /**
     * コンストラクタ
     */
    public QueueClient() {
    }

    /**
     * キューからデータを取得します。<br>
     * このメソッドはデータが取得できるまでスレッドをロックします。
     * 
     * @throws IOException
     */
    public byte[] pop() throws IOException {

        final File socketFile = new File(new File(
                System.getProperty(QueueConfig.DOMAIN_SOCKET_DIR_KEY)),
                QueueConfig.DOMAIN_SOCKET_FILE_FOR_POP);

        AFUNIXSocket sock = AFUNIXSocket.newInstance();
        sock.connect(new AFUNIXSocketAddress(socketFile));

        // データ読み込み
        InputStream is = sock.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        while (true) {
            int n = is.read(buf);
            if (n < 0)
                break;
            baos.write(buf, 0, n);
        }

        byte data[] = baos.toByteArray();

        // ソケット接続の後処理
        is.close();
        sock.close();

        return data;
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
