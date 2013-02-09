package jp.lg.ishinomaki.city.mrs.analyzer;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import jp.lg.ishinomaki.city.mrs.receiver.ReceiverConfig;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BCHTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ReceiverConfig.getInstance().loadYml("test/config/receiver.yml");
        // 0100010100000000000000001110010100001000010100000000000000000000001100000001001110110111100000100000000000000000000000000000000000000000000000000000000000000000
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * 2進数文字列をbyte[]にえ変換する（テスト用メソッド）
     * 
     * @param binary
     * @return
     */
    public static byte[] asByteArray(String binary) {
        // 文字列長の1/8の長さのバイト配列を生成
        // 2進数の8文字分(8ビット)が1バイト分に相当するため
        byte[] bytes = new byte[binary.length() / 8];

        // バイト配列の要素数分、処理を繰り返す。
        for (int index = 0; index < bytes.length; index++) {
            // 2進数文字列をバイトに変換して配列に格納。
            bytes[index] = (byte) Integer.parseInt(
                    binary.substring(index * 8, (index + 1) * 8), 2);
        }
        // バイト配列を返す。
        return bytes;
    }

    @Test
    public void バージョン番号1() {
        byte[] bchSrc = asByteArray("0001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getVersionNo(), is(1));
    }

    @Test
    public void バージョン番号2() {
        byte[] bchSrc = asByteArray("0010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getVersionNo(), is(2));
    }

    @Test
    public void バージョン番号3() {
        byte[] bchSrc = asByteArray("0011000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getVersionNo(), is(3));
    }

    @Test
    public void バージョン番号4() {
        byte[] bchSrc = asByteArray("0100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getVersionNo(), is(4));
    }

    @Test
    public void バージョン番号5() {
        byte[] bchSrc = asByteArray("0101000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getVersionNo(), is(5));
    }

    @Test
    public void BCH長() {
        byte[] bchSrc = asByteArray("0000010100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getBchLength(), is(20));
    }

    @Test
    public void 電文順序番号() {
        byte[] bchSrc = asByteArray("0000000000001111111111000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);

        // ------ 以下は電文順序番号の計算 -------
        // 2進化10進数(4bitで1つの10進数数値)で5桁の数値を表現しているため数値に変換する
        StringBuilder sb = new StringBuilder();
        int begin_index = 0;
        int end_index = 4;
        // 文字列を4文字(4bit)づつ取得し10進数数値に変換し文字列としてsbに追加
        for (int i = 0; i < 5; i++) {
            String substr = "11111111110000000000".substring(begin_index,
                    end_index);
            // 2進化文字列をintに変換
            int bcd = Integer.parseInt(substr, 2);
            sb.append(String.valueOf(bcd));
            begin_index += 4;
            end_index += 4;
        }
        // 最後にsbの内容をまとめて10進数int型に変換
        int expected = Integer.parseInt(sb.toString());
        // ------------------------------------
        assertThat(bch.getSequenceNo(), is(expected));
    }

    @Test
    public void 中継種別フラグ() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getRelayType(), is(1));
    }

    @Test
    public void 地震津波フラグ() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getEmergencyType(), is(1));
    }

    @Test
    public void テストタイプ() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getTestType(), is(1));
    }

    @Test
    public void XMLタイプ() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000001100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getXmlType(), is(3));
    }

    @Test
    public void データ機密度() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000011000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getClassificationLevel(), is(3));
    }

    @Test
    public void データ属性() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000000111100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getDataAttribute(), is(15));
    }

    @Test
    public void 気象庁内情報() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000000000011110000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getAgency(), is(15));
    }

    @Test
    public void データ種別() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000000000000001010010100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getMajorDataType(), is(10));
        assertThat(bch.getMinorDataType(), is(5));
    }

    @Test
    public void 電文情報BIF内の再送フラグ() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getBifResendType(), is(1));
    }

    @Test
    public void 電文情報BIF内のデータ属性() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000000000000000000000000000000011100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getBifDataAttribute(), is(7));
    }

    @Test
    public void 電文情報BIF内のデータ種別() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000000000000000000000000000000000011110000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getBifDataType(), is(15));
    }

    @Test
    public void AN桁数() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000000000000000000000000000000000000001111111100000000000000000000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getAnLength(), is(255));
    }

    @Test
    public void QCチェックサム() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000000000000000000000000000000000000000000000011111111111111110000000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getQcChecksum(), is("1111111111111111"));
    }

    @Test
    public void 発信官署番号の大分類() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001100000000000000000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getSendNoClassification(), is(3));
    }

    @Test
    public void 発信官署番号のシステム識別フラグ() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000011111111111111000000000000000000000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getSendNoIdentifier(), is("11111111111111"));
    }

    @Test
    public void 発信官署番号の端末番号() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000111111111111111100000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getSendNoTerminal(), is("1111111111111111"));
    }

    @Test
    public void 着信官署番号の大分類() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000011000000000000000000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getReceiveNoClassification(), is(3));
    }

    @Test
    public void 着信官署番号のシステム識別フラグ() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000111111111111110000000000000000");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getReceiveNoIdentifier(), is("11111111111111"));
    }

    @Test
    public void 着信官署番号の端末番号() {
        byte[] bchSrc = asByteArray("0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001111111111111111");
        BCH bch = new BCH(bchSrc);
        assertThat(bch.getReceiveNoTerminal(), is("1111111111111111"));
    }
}
