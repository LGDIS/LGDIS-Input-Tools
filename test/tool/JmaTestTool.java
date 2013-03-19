package tool;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;
import jp.lg.ishinomaki.city.mrs.pickup.JmaXmlDataHandler;
import jp.lg.ishinomaki.city.mrs.pickup.KsnXmlDataHandler;
import jp.lg.ishinomaki.city.mrs.pickup.PickupDataHandler;
import jp.lg.ishinomaki.city.mrs.pickup.TarDataHandler;

public class JmaTestTool {

    private JFrame mainFrame = null;
    private JFileChooser fileChooser = null;

    private JRadioButton jmaButton = null;
    private JRadioButton jalXmlButton = null;
    private JRadioButton jalTarButton = null;
    private JRadioButton ksnButton = null;

    private JRadioButton normalButton = null;
    private JRadioButton trainingButton = null;
    private JRadioButton testButton = null;

    /**
     * コンストラクタ
     */
    public JmaTestTool() {

        // 構成ファイル読み込み
        ParserConfig config = ParserConfig.getInstance();
        try {
            config.loadYml("config/parser.yml");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // メインフレーム
        mainFrame = new JFrame("JMAテストツール"); // フレームタイトル
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 閉じるボタンでアプリ終了
        // スクリーンサイズから表示位置を決定する
        int frameWidth = 550;
        int frameHeight = 200;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setBounds((screenSize.width - frameWidth) / 2,
                (screenSize.height - frameHeight) / 2, frameWidth, frameHeight);

        // メインコンテナ
        JPanel container = new JPanel();
        container.setLayout(new GridLayout(4, 1));

        // -------------------------
        // 入力元
        // -------------------------
        // グループ
        ButtonGroup inputGroup = new ButtonGroup();
        // ラジオボタン
        jmaButton = new JRadioButton("JMA(XML)");
        jmaButton.setSelected(true);
        jalXmlButton = new JRadioButton("J-Alert(XML)");
        jalTarButton = new JRadioButton("J-Alert(TAR)");
        ksnButton = new JRadioButton("河川(XML)");
        inputGroup.add(jmaButton);
        inputGroup.add(jalXmlButton);
        inputGroup.add(jalTarButton);
        inputGroup.add(ksnButton);

        JPanel inputPanel = new JPanel();
        inputPanel.add(jmaButton);
        inputPanel.add(jalXmlButton);
        inputPanel.add(jalTarButton);
        inputPanel.add(ksnButton);

        // -------------------------
        // 通常/訓練/テスト
        // -------------------------
        // グループ
        ButtonGroup modeGroup = new ButtonGroup();
        // ラジオボタン
        normalButton = new JRadioButton("通常");
        normalButton.setSelected(true);
        trainingButton = new JRadioButton("訓練");
        testButton = new JRadioButton("試験");
        modeGroup.add(normalButton);
        modeGroup.add(trainingButton);
        modeGroup.add(testButton);

        JPanel modePanel = new JPanel();
        modePanel.add(normalButton);
        modePanel.add(trainingButton);
        modePanel.add(testButton);

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

                // FileChooser表示
                if (fileChooser == null) {
                    fileChooser = new JFileChooser();
                }
                // File選択
                int selected = fileChooser.showOpenDialog(mainFrame);
                if (selected == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    byte[] contents = JmaTestTool.byteFromFile(file);

                    // -------------------------------------------
                    // ラジオボタンの選択によりハンドラークラスを分ける
                    // -------------------------------------------
                    // モード
                    int mode = 0;
                    if (normalButton.isSelected()) {
                        mode = 0;
                    } else if (trainingButton.isSelected()) {
                        mode = 1;
                    } else {
                        mode = 2;
                    }

                    PickupDataHandler handler = null;
                    if (jmaButton.isSelected()) {
                        handler = new JmaXmlDataHandler(mode, "JMA");
                    } else if (jalTarButton.isSelected()) {
                        handler = new TarDataHandler(mode, "JAL");
                    } else if (jalXmlButton.isSelected()) {
                        handler = new JmaXmlDataHandler(mode, "JAL");
                    } else if (ksnButton.isSelected()) {
                        handler = new KsnXmlDataHandler(mode, "KSN");
                    }
                    handler.handle(contents);
                }
            }
        });
        choosePanel.add(chooseButton);

        JPanel footerPanel = new JPanel();

        container.add(inputPanel);
        container.add(modePanel);
        container.add(choosePanel);
        container.add(footerPanel);

        mainFrame.getContentPane().add(container);
        mainFrame.setVisible(true);
    }

    /**
     * メイン関数
     * 
     * @param args
     */
    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(false);
        // コンストラクタ実行
        // 画面表示
        new JmaTestTool();
    }

    /**
     * Fileを読み込みbyte配列を取得する
     * 
     * @param file
     * @return
     */
    public static byte[] byteFromFile(File file) {

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
