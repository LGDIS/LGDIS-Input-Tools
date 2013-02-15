package jp.lg.ishinomaki.city.mrs.queue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

/**
 * キュー管理機能のアウトプット部分のソケットサーバとなるクラスです
 * 
 */
public class QueuePopServer extends Thread {

    // ログ用
    private final Logger log = Logger.getLogger(QueuePopServer.class
            .getSimpleName());

    /**
     * コンストラクタです.<br>
     */
    public QueuePopServer() {
    }

    /**
     * ソケット接続待ちスレッド開始.<br>
     * 
     */
    public void run() {
        new SocketAcceptThread().start();
    }

    /**
     * ソケット接続待ちスレッド用クラス
     */
    private class SocketAcceptThread extends Thread {

        private SocketOutputThread outputThread = null;

        @Override
        public void run() {

            // ドメインソケット用定義取得
            QueueConfig config = QueueConfig.getInstance();
            String sockDir = config.getDomainSocketDir();
            String popFile = config.getDomainSocketPopFile();

            File socketFile = new File(new File(sockDir), popFile);

            // ------------------------------------------------------
            // サーバソケット生成
            // ------------------------------------------------------
            AFUNIXServerSocket server = null;
            try {
                server = AFUNIXServerSocket.newInstance();
                server.bind(new AFUNIXSocketAddress(socketFile));
            } catch (Exception e) {
                // サーバソケットのバインドに失敗した場合はエラーログを出力して処理終了
                // 設定等の見直しが必要
                e.printStackTrace();
                log.severe("サーバソケットのバインドに失敗しました。設定が間違っている可能性があります。");
                return;
            }
            log.finest("server : " + server);

            // スレッド割り込みが発生するまでループ
            while (!Thread.interrupted()) {
                // -----------------------------------------
                // accept設定
                // クライアントの要求を受信待ち
                // クライアントから初めて接続された際にSocketインスタンスを取得
                // -----------------------------------------
                Socket socket = null;
                try {
                    // クライアントからの接続待ち
                    socket = server.accept();

                    // すでに受信スレッドが稼働している場合は受信スレッドを破棄する
                    if (outputThread != null) {
                        outputThread.interrupt();
                        outputThread = null;
                    }

                } catch (SocketException e) {
                    log.warning("接続の待機中にソケットエラーが発生したようです -> "
                            + e.getLocalizedMessage());
                    e.printStackTrace();
                    continue;
                } catch (IOException e) {
                    log.warning("接続の待機中に入出力エラーが発生したようです -> "
                            + e.getLocalizedMessage());
                    e.printStackTrace();
                    continue;
                } catch (Exception e) {
                    log.warning("サーバーソケット接続待ち時にエラー発生!! -> "
                            + e.getLocalizedMessage());
                    e.printStackTrace();
                    continue;
                }

                // ---------------------------------
                // データ出力用スレッド開始
                // ---------------------------------
                outputThread = new SocketOutputThread(socket);
                outputThread.start();

            }

        }
    }

    /**
     * キュー監視+データ送信処理用スレッド
     */
    private class SocketOutputThread extends Thread {

        /**
         * 出力先のソケットインスタンス
         */
        private Socket socket;

        /**
         * コンストラクタ
         * 
         * @param socket
         *            ソケットインスタンス
         */
        public SocketOutputThread(Socket socket) {
            this.socket = socket;
        }

        /**
         * ソケットデータ受信スレッド開始.<br>
         * 
         */
        @Override
        public void run() {

            OutputStream os = null;
            try {
                // キュー管理されているデータ取得
                QueueManager queueManager = QueueManager.getInstance();
                // キューにアイテムが存在しない場合はキューにアイテムが挿入されるまでスレッドをロックします
                byte[] data = queueManager.pop();

                // キューからポップしたデータをクライアントに送信
                os = socket.getOutputStream();
                os.write(data);
                os.flush();

            } catch (InterruptedException ie) {
                // SocketAcceptThreadの処理内においてソケット接続許可が実施された場合に既に存在するSocketOutputThreadに対してInterrupt()が発行されます。
                // この場合に等runメソッド内(具体的にはQueueManager.popt()メソッド内)でInterruptedExceptionが発生します。
                // 意図した例外のため特にエラーメッセージなどは発行しません。
                log.finest("run()メソッド内で例外発生！ e -> " + ie.toString());
                return;
            } catch (Exception e) {
                // 上記以外の例外発生
                e.printStackTrace();
                log.severe("キューからのアイテム取得待ち時に例外発生。キュー監視処理を中断します。");
                return;
            } finally {
                // アウトプットストリームを閉じる
                // ソケットを閉じる
                try {
                    if (os != null) {
                        os.close();
                    }
                    if (socket != null) {
                        socket.close();
                    }
                } catch (Exception e) {
                }
            }
        }

    }

}
