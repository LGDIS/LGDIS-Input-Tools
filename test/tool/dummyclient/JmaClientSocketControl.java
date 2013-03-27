package tool.dummyclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Jma手順を使用したソケット接続制御のうち送信側の制御を行います。
 */
public class JmaClientSocketControl {

    /**
     * 送信先ホストのIPアドレス
     */
    private String sIpAddress;

    /**
     * 送信先のポート番号
     */
    private int iPortNo;

    /**
     * 接続で使用するSocketインスタンス
     */
    private Socket socket = null;

    /**
     * 接続で使用するOutputStreamインスタンス
     */
    private OutputStream outputStream = null;

    /**
     * 接続で使用するBufferedOutputStreamインスタンス
     */
    private BufferedOutputStream bufferedOutputStream = null;

    /**
     * 接続で使用するInputStreamインスタンス
     */
    private InputStream inputStream = null;

    /**
     * 接続で使用するBufferedInputStreamインスタンス
     */
    private BufferedInputStream bufferedInputStream = null;

    /**
     * デリゲートインスタンス
     */
    private JmaClientSocketControlDelegate delegate = null;

   /**
    * データ受信スレッド
    */
    private ClientSocketReceiveThread receiveThread = null;

    /**
     * コンストラクタ
     */
    public JmaClientSocketControl(String ipAddress, int portNo) {
        super();
        this.sIpAddress = ipAddress;
        this.iPortNo = portNo;
    }

    public void setDelegate(JmaClientSocketControlDelegate delegate) {
        this.delegate = delegate;
    }

    public JmaClientSocketControlDelegate getDelegate() {
        return this.delegate;
    }

    /**
     * サーバへの接続要求
     * 
     * @return Socket 接続成功の場合は接続時のSocketインスタンスを返却 接続失敗の場合はnullを返却
     */
    public Socket connect() {

        try {
            // ソケットインスタンス生成
            this.socket = new Socket(this.sIpAddress, this.iPortNo);

            // 各種ストリームを保持
            this.inputStream = this.socket.getInputStream();
            this.bufferedInputStream = new BufferedInputStream(this.inputStream);
            this.outputStream = this.socket.getOutputStream();
            this.bufferedOutputStream = new BufferedOutputStream(
                    this.outputStream);

            // ---------------------------------
            // データ受信スレッド開始
            // ---------------------------------
            receiveThread = new ClientSocketReceiveThread();
            receiveThread.start();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return this.socket;
    }

    /**
     * メッセージを送信します。 サーバからメッセージを受信した場合はそのメッセージを返却します。
     * 
     * @param msg
     *            送信メッセージ
     * @return String 受信メッセージ
     */
    public boolean send(byte[] data) {

        try {
            // メッセージ送信
            this.bufferedOutputStream.write(data);
            this.bufferedOutputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    
    /**
     * 接続解除
     * 
     * @return
     */
    public boolean close() {
        try {
            bufferedInputStream.close();
            inputStream.close();
            bufferedOutputStream.close();
            outputStream.close();
            socket.close();
            
            // スレッド停止
            if (receiveThread == null) {
                receiveThread.setIsLoop(false);
                receiveThread.interrupt();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * データ受信用スレッド
     */
    public class ClientSocketReceiveThread extends Thread {

        /**
         * データ受信処理のループを制御
         */
        private boolean isLoop = true;

        /**
         * コンストラクタ
         */
        public ClientSocketReceiveThread() {
        }

        /**
         * データ受信のループを制御するフラグを設定
         * 
         * @param isLoop
         *            true ループする false ループしない
         */
        public void setIsLoop(boolean isLoop) {
            this.isLoop = isLoop;
        }

        /**
         * データ受信開始
         */
        public void run() {
            while (isLoop) {

                // データ受信待ち
                boolean ret = receiveData();

                // データ受信に失敗した場合はSocketを切断してループ終了
                if (ret == false) {
                    if (isLoop == true) {
                        close();
                        break;
                    }
                }
            }
        }

        /**
         * データを受信
         * 
         * @return boolean true 受信成功 false 失敗
         */
        public boolean receiveData() {

            byte[] response = null;
            try {
                // 受信したデータを読み込む( BuffredInputStreamを使用 )
                byte[] temp = new byte[10000];
                int readLen = bufferedInputStream.read(temp, 0, 10000);

                // 受信データ長が-1の場合はコネクション切断の可能性あり
                if (readLen == -1) {
                    return false;
                }

                // 受信データを作業用バッファ[data]にコピー
                response = new byte[readLen];
                System.arraycopy(temp, 0, response, 0, readLen);

                // デリゲート通知
                delegate.receiveData(response);

            } catch (IOException e) {
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }

            return true;
        }
    }
}
