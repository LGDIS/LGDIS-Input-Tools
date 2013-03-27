package tool.dummyclient;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

public class DummyClientMain implements JmaClientSocketControlDelegate {

    private JFrame mainFrame = null;
    private JTextField serverField = null;
    private JTextField portField = null;
    private JTextArea responseText = null;

    private JmaClientSocketControl socketClient = null;

    private JFileChooser fileChooser = null;

    /**
     * コンストラクタ
     */
    public DummyClientMain() {

        // メインフレーム
        mainFrame = new JFrame("dummyJMAClient"); // フレームタイトル
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 閉じるボタンでアプリ終了
        // スクリーンサイズから表示位置を決定する
        int frameWidth = 400;
        int frameHeight = 470;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setBounds((screenSize.width - frameWidth) / 2,
                (screenSize.height - frameHeight) / 2, frameWidth, frameHeight);

        // メインコンテナ
        JPanel container = new JPanel();

        // IPアドレスフィールド
        JPanel serverPanel = new JPanel();
        JLabel serverLabel = new JLabel("IPアドレス  ");
        serverField = new JTextField(20);
        serverField.setText("localhost"); // 初期値設定
        serverPanel.add(serverLabel);
        serverPanel.add(serverField);
        container.add(serverPanel);

        // ポート番号
        JPanel portPanel = new JPanel();
        JLabel portLabel = new JLabel("ポート番号");
        portField = new JTextField(20);
        portField.setText("30000"); // 初期設定
        portPanel.add(portLabel);
        portPanel.add(portField);
        container.add(portPanel);

        // -------------------------
        // 接続開始ボタン
        // -------------------------
        JPanel connectStartPanel = new JPanel();
        JButton connectStartButton = new JButton("サーバとの接続開始");
        connectStartButton.setPreferredSize(new Dimension(300, 40));
        // delegate設定用に一時的に自身を変数に保持
        final DummyClientMain self = this;
        connectStartButton.addActionListener(new ActionListener() {
            // 接続開始ボタンクリック時処理(匿名クラスで実装)
            @Override
            public void actionPerformed(ActionEvent e) {
                if (socketClient == null) {
                    // ソケット制御を生成して接続要求
                    String server = serverField.getText();
                    String port = portField.getText();
                    int iPort = Integer.valueOf(port);
                    socketClient = new JmaClientSocketControl(server, iPort);
                    socketClient.setDelegate(self);
                    socketClient.connect();
                    // 応答電文表示域をクリア
                    responseText.setText("サーバとの接続に成功");
                }
            }
        });
        connectStartPanel.add(connectStartButton);
        container.add(connectStartPanel);

        // -------------------------
        // 接続切断ボタン
        // -------------------------
        JPanel connectEndPanel = new JPanel();
        JButton connectEndButton = new JButton("サーバとの接続を切断");
        connectEndButton.setPreferredSize(new Dimension(300, 40));
        connectEndButton.addActionListener(new ActionListener() {
            // 切断ボタンクリック時処理(匿名クラスで実装)
            @Override
            public void actionPerformed(ActionEvent e) {
                // サーバ接続未済の場合はクリック無効
                if (socketClient == null) {
                    responseText.setText("サーバ接続未済");
                    return;
                }
                socketClient.close();
                socketClient = null;
                responseText.setText("サーバとの接続を切断");
            }
        });
        connectEndPanel.add(connectEndButton);
        container.add(connectEndPanel);

        // -------------------------
        // 電文選択ボタン
        // -------------------------
        JPanel choosePanel = new JPanel();
        JButton chooseButton = new JButton("電文選択");
        chooseButton.setPreferredSize(new Dimension(300, 40));
        chooseButton.addActionListener(new ActionListener() {
            // 電文選択ボタンクリック時処理
            @Override
            public void actionPerformed(ActionEvent e) {

                // サーバ接続未済の場合はクリック無効
                if (socketClient == null) {
                    responseText.setText("サーバ未接続");
                    return;
                }

                // FileChooser表示
                if (fileChooser == null) {
                    fileChooser = new JFileChooser();
                }
                // File選択
                int selected = fileChooser.showOpenDialog(mainFrame);
                if (selected == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    byte[] data = createData(file);
                    // 選択したFileの内容を送信
                    boolean ret = socketClient.send(data);
                    if (!ret) {
                        responseText.setText("送信失敗!");
                    }
                }
            }
        });
        choosePanel.add(chooseButton);
        container.add(choosePanel);
        // 応答電文表示
        JPanel responsePanel = new JPanel();
        responseText = new JTextArea(10, 30);
        EtchedBorder border = new EtchedBorder(EtchedBorder.RAISED);
        responseText.setBorder(border);
        responseText.setMargin(new Insets(5, 10, 5, 10));
        responsePanel.add(responseText);
        container.add(responsePanel);

        mainFrame.getContentPane().add(container);

        mainFrame.setVisible(true);
    }

    /**
     * メイン関数
     * 
     * @param args
     */
    public static void main(String[] args) {
        // コンストラクタ実行
        // 画面表示
        new DummyClientMain();
    }

    /**
     * 受信データ通知
     */
    @Override
    public void receiveData(byte[] data) {
        // 受信データ内容をテキストエリアに表示
        responseText.setText(new String(data));
    }

    /**
     * 電文作成用メソッド<br>
     * 引数で指定したFileのデータを読み込みバイナリデータを返却します。<br>
     * 
     * @param file
     * @return
     */
    public static byte[] createData(File file) {

        byte[] result = null;
        try {
            int dataLength = (int) file.length();
            result = new byte[dataLength];
            FileInputStream in = new FileInputStream(file);
            int ch;
            int i = 0;
            while ((ch = in.read()) != -1) {
                result[i] = (byte) ch;
                i++;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
