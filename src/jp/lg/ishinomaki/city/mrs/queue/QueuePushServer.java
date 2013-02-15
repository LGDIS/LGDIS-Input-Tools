package jp.lg.ishinomaki.city.mrs.queue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Logger;

import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

/**
 * キュー管理機能のインプットサーバとなるクラスです.
 * 
 */
public class QueuePushServer extends Thread {

    /**
     * ログ用
     */
    private final Logger log = Logger.getLogger(QueuePushServer.class
            .getSimpleName());

    /**
     * コンストラクタ.<br>
     */
    public QueuePushServer() {
    }

    /**
     * スレッド開始.<br>
     * 
     */
    public void run() {

        // ドメインソケット用定義取得
        QueueConfig config = QueueConfig.getInstance();
        String sockDir = config.getDomainSocketDir();
        String pushFile = config.getDomainSocketPushFile();
        
        // ドメインソケットファイル取得
        final File socketFile = new File(new File(sockDir), pushFile);

        // ------------------------------------------------------
        // サーバソケット生成
        // ------------------------------------------------------
        // ソケットサーバインスタンス生成
        AFUNIXServerSocket server = null;
        try {
            server = AFUNIXServerSocket.newInstance();
            server.bind(new AFUNIXSocketAddress(socketFile));
        } catch (IOException e) {
            // サーバソケットのバインドに失敗した場合はエラーログを出力して処理終了
            // 設定等の見直しが必要
            e.printStackTrace();
            log.severe("サーバソケットのバインドに失敗しました。設定が間違っている可能性があります。");
            return;
        }
        log.finest("server : " + server);

        // スレッドが割り込まれるまでループ
        while (!Thread.interrupted()) {

            // データ保存用変数
            byte data[] = null;

            Socket sock = null;
            InputStream is = null;
            try {
                // ソケットは接続の度に生成
                sock = server.accept();
                is = sock.getInputStream();

                // クライアントからの送信データ読み込み
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // 1024(1k)バイトずつデータを読み込み
                byte[] buf = new byte[1024];
                while (true) {
                    int n = is.read(buf);
                    if (n < 0)
                        break;
                    baos.write(buf, 0, n);
                }
                data = baos.toByteArray();
                log.finest("データを受信しました。データサイズ:" + data.length);

            } catch (IOException e) {
                e.printStackTrace();
                log.severe("ソケットによるデータ受信時に例外が発生しました。");
            } finally {
                // インプットストリームを閉じる
                // ソケットを閉じる
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (sock != null) {
                        sock.close();
                    }
                } catch (IOException e) {
                }
            }

            // 受信したデータをキューに登録
            QueueManager queueManager = QueueManager.getInstance();
            try {
                queueManager.push(data);
            } catch (Exception e) {
                e.printStackTrace();
                log.severe("受信したデータをキューに登録する際に例外が発生しました。");
            }
        }
    }
}
