package tool.dummyclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * テスト用 JMA受信サーバに対する送信電文を作成
 * 
 */
public class DummyClientDataFactory {

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
