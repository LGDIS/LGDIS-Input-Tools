//
//  JmaServerSocketControl.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.receiver.jma;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * JMA接続手順を使用したソケット接続制御を担当します。<br>
 * JMA接続手順の"ヘルスチェック受信/応答"と"チェックポイント管理"と"重複電文削除"機能を行います。<br>
 * ユーザデータを受信した場合は当クラスにデリゲート設定されたクラスへ受信データを通知します。<br>
 * ソケット接続待ちとデータ受信待ちを別スレッドで行うことでクライアントからの後接続優先制御を実現します。<br>
 * 
 */
public class JmaServerSocketControl {

    /**
     * 当クラスのロガーインスタンス
     */
    private final Logger log = Logger.getLogger(JmaServerSocketControl.class
            .getSimpleName());

    /**
     * このクラスを起動したスレッド名
     */
    private String threadName = "";

    /**
     * 受信サーバのIPアドレス IPアドレスの文字列表現 (ex. "192.168.0.1")
     */
    private String ipAddress = null;

    /**
     * 受信待ちするポート番号
     */
    private int portNo = 0;

    /**
     * Java提供のServerSocketインスタンス
     */
    ServerSocket serverSocket = null;

    /**
     * 接続に使用するSocketインスタンス
     */
    Socket socket = null;

    /**
     * 接続中のInputStream
     */
    InputStream inputStream = null;

    /**
     * 接続中のBufferedInputStream
     */
    BufferedInputStream bufferedInputStream = null;

    /**
     * 接続中のOutputStream
     */
    OutputStream outputStream = null;

    /**
     * 接続中のBufferedOutputStream
     */
    BufferedOutputStream bufferedOutputStream = null;

    /**
     * 各イベントの通知先
     */
    private JmaServerSocketControlDelegate delegate = null;

    /**
     * 接続待ちスレッド
     */
    private ServerSocketAcceptThread acceptThread = null;

    /**
     * データ受信スレッド
     */
    private ServerSocketReceiveThread receiveThread = null;

    /**
     * 分割電文を受信した際の電文保管用変数
     */
    JmaMessage savedMessage = null;

    /**
     * チェックポイント管理対象のデータを保存するリスト
     */
    List<JmaMessage> checkpointManagedMessages = new ArrayList<JmaMessage>();

    /**
     * キューの最大値 ServerSocketコンストラクタの第二引数(backlog)に渡す値
     * 通常の電文とヘルスチェック用の電文を受信するため2以上の十分な値を指定 以下JavaDoc引用 ----- 受信する接続 (接続要求)
     * のキューの最大長は、backlog パラメータに設定されます。 キューが埋まっているときに接続要求があると、接続は拒否されます。
     * --------------------
     */
    private int queMax = 50;

    /**
     * 1度に受信するデータのMAXサイズ
     */
    private static int RECEIVE_DATA_MAXSIZE = 720010;

    /**
     * Socket制御クラスのコンストラクターです。
     */
    public JmaServerSocketControl(String ipAddress, int portNo) {
        super();
        this.ipAddress = ipAddress;
        this.portNo = portNo;

    }

    /**
     * デリゲート設定
     * 
     * @param delegate
     *            デリゲートインスタンス
     */
    public void setDelegate(JmaServerSocketControlDelegate delegate) {
        this.delegate = delegate;
    }

    /**
     * デリゲートインスタンス取得
     * 
     * @return JmaServerSocketControlDelegate デリゲートインスタンス
     */
    public JmaServerSocketControlDelegate getDelegate() {
        return this.delegate;
    }

    /**
     * スレッド名を設定します。スレッド名はログ出力時に使用します。
     * 
     * @param threadName
     *            スレッド名
     */
    public void setThreadName(String threadName) {
        if (threadName == null) {
            return;
        }
        this.threadName = threadName;
    }

    /**
     * スレッド名を取得します。
     * 
     * @return String スレッド名
     */
    public String getThreadName() {
        return this.threadName;
    }

    /**
     * サーバーソケットインスタンス作成<br>
     * インスタンス化が済んだ時点でポートの監視を開始<br>
     * 
     * @return true:成功 false:失敗
     */
    public boolean setup() {
        // -----------------------------------------
        // サーバーソケットインスタンスの生成
        // インスタンス化が済んだ時点でポートの監視を開始
        // -----------------------------------------
        try {
            log.finest("[" + this.threadName + "] サーバソケット作成開始 IPアドレス["
                    + ipAddress + "] ポート番号[" + portNo + "]");

            // サーバソケットのバインド先ローカルアドレス
            InetAddress myAddress = InetAddress.getByName(ipAddress);

            this.serverSocket = new ServerSocket(this.portNo, this.queMax,
                    myAddress);

            // ソケットインスタンス生成成功ログ表示
            log.finest("[" + this.threadName + "] サーバソケット作成完了 -> "
                    + this.serverSocket.toString());

        } catch (SecurityException e) {

            log.severe("["
                    + this.threadName
                    + "] セキュリティーマネージャーが存在し、その checkListen メソッドがこの操作を許可しないようです -> "
                    + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        } catch (IOException e) {

            log.severe("[" + this.threadName
                    + "] ソケットを開いているときに入出力エラーが発生したようです -> "
                    + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }

        // 接続待ち開始
        acceptThread = new ServerSocketAcceptThread();
        acceptThread.start();

        return true;
    }

    /**
     * 接続中のSocketをcloseする。 クライアントから切断された場合はサーバ側でもこのメソッドを実行する。その後再度接続待ちへ遷移する。
     */
    public void closeSocket() {
        if (this.socket == null) {
            return;
        }

        // 一時保存注のデータをクリア
        this.savedMessage = null;

        try {
            // ストリームとソケットをクローズ
            this.bufferedInputStream.close();
            this.inputStream.close();
            this.bufferedOutputStream.close();
            this.outputStream.close();

            // 切断するSocketの接続先IPアドレスをログ出力
            InetAddress iNetAddress = socket.getInetAddress();
            String address = iNetAddress.getHostName();
            log.info("[" + threadName + "] " + address + " との接続が切断されました。");

            this.socket.close();

            // インスタンスクリア
            this.bufferedInputStream = null;
            this.inputStream = null;
            this.bufferedOutputStream = null;
            this.outputStream = null;
            this.socket = null;

        } catch (IOException e) {
            log.warning("[" + this.threadName + "] ソケットクローズエラー IOException->"
                    + e);
            e.printStackTrace();
        }
    }

    /**
     * サーバーソケットを切断します。<br>
     * データ受信待ち/ソケット接続待ちのソケットもすべてクローズします。<br>
     * このメソッドを実行すると以降接続待ちのループは発生しません。再度ソケット接続待ちにするには当クラスの生成から実施してください。
     */
    public void closeServerSocket() {
        try {
            // ループフラグをOFFにしてスレッド中断
            if (this.acceptThread != null) {
                this.acceptThread.setIsLoop(false);
                this.acceptThread.interrupt();
            }
            if (this.receiveThread != null) {
                this.receiveThread.setIsLoop(false);
                this.receiveThread.interrupt();
            }

            // すべてのソケットとストリームをクリア
            this.closeSocket();

            // サーバソケットを切断
            this.serverSocket.close();
        } catch (Exception e) {
            log.warning("サーバーソケットクローズエラー IOException-> "
                    + e.getLocalizedMessage());
            e.printStackTrace();
        }
        this.serverSocket = null;
    }

    /**
     * Jma手順を使用したソケット接続制御のうち接続要求待ち(accept)の制御を行います。<br>
     * Jma手順において"後接続優先"の仕様を実現するため当スレッドクラスで常にaccept要求を待ちます。<br>
     */
    public class ServerSocketAcceptThread extends Thread {

        /**
         * ソケット接続待ち処理のループを制御
         */
        private boolean isLoop = true;

        /**
         * コンストラクタです。
         * 
         * @param isAcceptAlways
         * @param listener
         */
        public ServerSocketAcceptThread() {
        }

        /**
         * ソケット接続待ち処理のループを制御するフラグを設定
         * 
         * @param isLoop
         *            true ループする false ループしない
         */
        public void setIsLoop(boolean isLoop) {
            this.isLoop = isLoop;
        }

        /**
         * スレッド処理
         */
        public void run() {
            while (isLoop) {
                // -----------------------------------------
                // accept設定
                // クライアントの要求を受信待ち
                // クライアントから初めて接続された際にSocketインスタンスを取得
                // -----------------------------------------
                Socket newSocket = null;
                try {
                    log.info("[" + threadName + "] 接続要求待ち...");

                    newSocket = serverSocket.accept(); // クライアントからの接続待ち

                    // 接続元情報取得
                    InetAddress iNetAddress = newSocket.getInetAddress();
                    String address = iNetAddress.getHostName();
                    log.info("[" + threadName + "] " + address
                            + " からの接続要求を許可しました。");

                    // すでに受信スレッドが稼働している場合は受信スレッドを破棄する
                    if (receiveThread != null) {
                        receiveThread.setIsLoop(false);
                        receiveThread = null;
                    }

                    // 現在接続中のSokcetが存在する場合は該当Socketは破棄する
                    closeSocket();

                    // 新規接続ソケットから入出力ストリームを取得
                    inputStream = newSocket.getInputStream();
                    bufferedInputStream = new BufferedInputStream(inputStream);
                    outputStream = newSocket.getOutputStream();
                    bufferedOutputStream = new BufferedOutputStream(
                            outputStream);

                } catch (SocketException e) {
                    // isLoopがfalseでSocketException発生は
                    // 明示的にソケットをクローズしているためエラーメッセージは表示しない
                    // isLoopがtrueの場合は念のためエラーメッセージ表示
                    if (this.isLoop) {
                        e.printStackTrace();
                    }
                    continue;
                } catch (IOException e) {
                    log.warning("[" + threadName
                            + "] 接続の待機中に入出力エラーが発生したようです -> "
                            + e.getLocalizedMessage());
                    e.printStackTrace();
                    continue;
                } catch (Exception e) {
                    log.warning("[" + threadName
                            + "] サーバーソケット接続待ち時にエラー発生!! -> "
                            + e.getLocalizedMessage());
                    e.printStackTrace();
                    continue;
                }

                // ---------------------------------
                // Socketに対する設定
                // ---------------------------------
                try {
                    // SocketにTCP/IPレベルのKEEPALIVEを設定
                    // JMA通信の仕様に従う
                    newSocket.setKeepAlive(true);

                    // SocketにTCP/IPレベルのREUEADDRを設定
                    // JMA通信仕様にはないが、切断後に再接続をスムーズに行うため
                    // この設定がない場合は4分ほど再接続できないらしい(TCPやOSの仕様により)
                    newSocket.setReuseAddress(true);

                } catch (SocketException e) {
                    log.warning("[" + threadName
                            + "] サーバーソケット接続待ち時にエラー発生!! -> "
                            + e.getLocalizedMessage());
                    e.printStackTrace();
                    continue;
                }

                // 接続開始を通知
                if (delegate != null) {
                    delegate.acceptConnection(newSocket);
                }

                // これから使用するSocketインスタンスを保持
                socket = newSocket;

                // ---------------------------------
                // データ受信スレッド開始
                // ---------------------------------
                receiveThread = new ServerSocketReceiveThread();
                receiveThread.start();

            }
        }
    }

    /**
     * データ受信用スレッド
     * 
     */
    public class ServerSocketReceiveThread extends Thread {

        /**
         * データ受信処理のループを制御
         */
        private boolean isLoop = true;

        /**
         * コンストラクタ
         */
        public ServerSocketReceiveThread() {
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

                        log.info("[" + threadName
                                + "] データ受信でエラーが発生したためソケット接続を切断します。");

                        closeSocket();
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
            log.info("[" + threadName + "] データ受信待ち...");

            // -----------------------------------------
            // クライアントからのデータ受信
            // -----------------------------------------
            JmaMessage msg = null; // 受信メッセージ
            try {
                // 受信したデータを読み込む( BuffredInputStreamを使用 )
                byte[] temp = new byte[RECEIVE_DATA_MAXSIZE];
                int readLen = bufferedInputStream.read(temp, 0,
                        RECEIVE_DATA_MAXSIZE);

                // 受信データ長が-1の場合はコネクション切断の可能性あり
                if (readLen == -1) {
                    return false;
                }

                log.info("[" + threadName + "] データを受信しました。データ長->[" + readLen
                        + "]");

                // 受信データを作業用バッファ[data]にコピー
                byte[] data = new byte[readLen];
                System.arraycopy(temp, 0, data, 0, readLen);

                // for test
                //saveFile(data);
                
                // ---------------------------------------------------------
                // 電文分割受信の場合は保存中のメッセージに追加する
                // ---------------------------------------------------------
                if (savedMessage != null) {

                    log.finest("[" + threadName + "] 電文分割受信中のため受信データを保存データに結合]");

                    // 保存されているデータに分割データを結合
                    boolean result = savedMessage.appendData(data);
                    if (result == false) {
                        // データの結合に失敗した場合はデータ自体破棄
                        log.warning("[" + threadName
                                + "] 結合データサイズがヘッダに記載のサイズより大きくなったためデータを破棄して処理中断");
                        return false;
                    }
                    msg = savedMessage;

                } else {
                    // JmaMessageを新規作成
                    msg = new JmaMessage(data);
                }

                log.finest("[" + threadName + "] データ解析結果 -> " + msg.toString());

                // ---------------------------------------------------------
                // 電文分割受信の場合は保存中のメッセージに追加する
                // ---------------------------------------------------------
                if (msg.isComplete() == false) {

                    log.finest("["
                            + threadName
                            + "] 電文長がヘッダの電文長より小さいため分割電文受信と判断。データを一時保存して次の電文受信を待つ。");
                    // データ保存
                    savedMessage = msg;

                    return true;
                }

                // --------------------------------------------------------------------------
                // 受信メッセージの種類により処理を分ける
                // 1.ヘルスチェックの場合はこの処理内で回答する
                // 2.ユーザデータ(チェックポイントあり)の場合はこの処理内で回答する
                // 3.ユーザデータ(チェックポイントなし)の場合はメッセージを呼び出し元へ渡して処理終了
                // --------------------------------------------------------------------------
                // メッセージ種別がJMA接続仕様対象外の場合はデータ自体無視する
                if (msg.isValidMessageType()) {

                    if (msg.isHealthCheck()) {
                        // 1.ヘルスチェック //
                        log.info("[" + threadName + "] ヘルスチェック電文です。");
                        // ヘルスチェック応答返却
                        ackHelthCheck();

                    } else if (msg.isCheckPoint()) {
                        // 2.チェックポイントあり //
                        log.info("[" + threadName
                                + "] ユーザデータ (チェックポイントあり) 電文です。");

                        // チェックポイントの場合はデータをインスタンス変数に保存しクライアントへ"ACK"を回答
                        appendCheckpointManagedData(msg);
                        // チェックポイント応答返却
                        ackCheckpoint(msg);

                    } else {
                        // 3.チェックポイントなし //
                        log.info("[" + threadName
                                + "] ユーザデータ (チェックポイントなし) 電文です。");

                        // 分割受信用保存データ初期化
                        savedMessage = null;

                        // ----------------------------------------------------------------------
                        // デリゲートにユーザデータ内容を通知
                        // チェックポイント管理されている場合は全ユーザデータを結合
                        // チェックポイント管理されていない場合は1つのメッセージのユーザデータを返却
                        // ----------------------------------------------------------------------
                        byte[] userData = null;
                        if (checkpointManagedMessages != null
                                && checkpointManagedMessages.size() > 0) {
                            // チェックポイント管理中
                            userData = mergeCheckpointManagedData();
                        } else {
                            // チェックポイント管理なし
                            userData = msg.getUserData();
                        }

                        // チェックポイント管理用変数初期化
                        checkpointManagedMessages = new ArrayList<JmaMessage>();

                        // デリゲート通知
                        delegate.receiveData(msg.getMessageType(), userData);
                    }

                } else {
                    log.severe("想定外のメッセージ種別です。 種別 -> " + msg.getMessageType());
                    System.err.println("想定外のメッセージ種別です。 種別 -> "
                            + msg.getMessageType());
                }

            } catch (IOException e) {
                log.warning("["
                        + threadName
                        + "] 入力ストリームの作成時に入出力エラーが発生したか、ソケットがクローズされているか、ソケットが接続されていないまたは shutdownInput() を使ってソケットの入力がシャットダウンされた可能性があります -> "
                        + e.getLocalizedMessage());
                e.printStackTrace();
                return false;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    /**
     * チェックポイント管理データに新規にメッセージを追加します。<br>
     * このメソッドは電文重複削除機能を実施します。
     * 
     * @param msg
     */
    void appendCheckpointManagedData(JmaMessage msg) {

        // チェックポイントデータ管理テーブル初期化
        if (checkpointManagedMessages == null) {
            checkpointManagedMessages = new ArrayList<JmaMessage>();
        }

        // チェックポイント管理対象のデータがない場合はテーブルに引数データを追加
        if (checkpointManagedMessages.size() == 0) {
            checkpointManagedMessages.add(msg);
        }
        // チェックポイント管理対象のデータが既にある場合は重複電文チェックを行う
        else {
            // チェックポイント管理されているJmaMssageのうち最後のものを取得
            JmaMessage lastMsg = checkpointManagedMessages
                    .get(checkpointManagedMessages.size() - 1);
            // byte配列の比較
            if (Arrays.equals(lastMsg.getData(), msg.getData())) {
                // byte配列が一致する場合は電文が重複していると判断
                log.warning("電文が重複しているため、新規に受信した電文は保存しません。");
            } else {
                // 電文が重複していない場合はチェックポイント管理にデータを追加する
                checkpointManagedMessages.add(msg);
            }
        }
    }

    /**
     * チェックポイント管理されている全メッセージのユーザデータ部を結合して返却します。
     * 
     * @return チェックポイント管理中の全ユーザデータ結合結果
     */
    byte[] mergeCheckpointManagedData() {

        int dataLength = 0;
        // チェックポイント管理されている全データのユーザデータ長を計算
        for (JmaMessage msg : checkpointManagedMessages) {
            dataLength = dataLength + msg.getUserDataLength();
        }

        byte[] result = new byte[dataLength];
        int currentPos = 0;
        // チェックポイント管理されている全データのユーザデータを結合
        for (JmaMessage msg : checkpointManagedMessages) {
            int length = msg.getUserDataLength();
            System.arraycopy(msg.getUserData(), 0, result, currentPos,
                    msg.getUserDataLength());
            currentPos = currentPos + length;
        }

        return result;
    }

    /**
     * 内部メソッド<br>
     * ヘルスチェックの応答を返す。複数箇所で使用するため内部メソッド化。
     * 
     * @throws Exception
     */
    void ackHelthCheck() {
        log.finest("ヘルスチェック応答前");
        try {
            bufferedOutputStream.write(JmaMessage.generateHelthcheckAck());
            bufferedOutputStream.flush();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.finest("ヘルスチェック応答後");
    }

    /**
     * 内部メソッド<br>
     * チェックポイントの応答を返す。複数箇所で使用するため内部メソッド化。
     * 
     * @param data
     *            受信電文
     */
    void ackCheckpoint(JmaMessage msg) {
        log.finest("チェックポイント応答前");
        try {
            bufferedOutputStream.write(msg.generateCheckPointAck());
            bufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.finest("チェックポイント応答後");
    }

    /**
     * テスト用メソッド<br>
     * 受信したデータをファイルに出力
     * 
     * @param data
     * @param fileName
     */
    void saveFile(byte[] data) {
        try {
            // ファイル名は現在日時を使用
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
            String fileName = sdf.format(new Date());
            FileOutputStream output = new FileOutputStream("/Users/igakuratakayuki/tmp/" + fileName);
            output.write(data,0,data.length);
            output.flush();
            output.close();
        } catch (Exception e) {
        }
        
    }
}
