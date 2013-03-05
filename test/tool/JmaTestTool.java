package tool;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import jp.lg.ishinomaki.city.mrs.parser.ParserConfig;
import jp.lg.ishinomaki.city.mrs.pickup.PdfDataHandler;
import jp.lg.ishinomaki.city.mrs.pickup.TextDataHandler;
import jp.lg.ishinomaki.city.mrs.pickup.JmaXmlDataHandler;

public class JmaTestTool {

    private JFrame mainFrame = null;
    private JFileChooser fileChooser = null;

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
        int frameWidth = 400;
        int frameHeight = 90;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setBounds((screenSize.width - frameWidth) / 2,
                (screenSize.height - frameHeight) / 2, frameWidth, frameHeight);

        // メインコンテナ
        JPanel container = new JPanel();

        // -------------------------
        // 電文選択ボタン
        // -------------------------
        JPanel directChoosePanel = new JPanel();
        JButton directChooseButton = new JButton("電文選択");
        directChooseButton.setPreferredSize(new Dimension(300, 40));
        directChooseButton.addActionListener(new ActionListener() {
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

                    // -------------------------------------------
                    // 拡張子によりデータを渡すハンドラークラスを分ける
                    // -------------------------------------------
                    byte[] contents = JmaTestTool.byteFromFile(file);
                    String fn = file.getName();
                    if (fn.endsWith("xml") || fn.endsWith("XML")) {
                        JmaXmlDataHandler handler = new JmaXmlDataHandler(0, "JAL");
                        handler.handle(contents);
                    } else if (fn.endsWith("pdf") || fn.endsWith("PDF")) {
                        PdfDataHandler handler = new PdfDataHandler(0, "JMA");
                        handler.handle(contents);
                    } else if (fn.endsWith("txt") || fn.endsWith("TXT")) {
                        TextDataHandler handler = new TextDataHandler(0, "JMA");
                        handler.handle(contents);
                    }
                }
            }
        });
        directChoosePanel.add(directChooseButton);
        container.add(directChoosePanel);

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
