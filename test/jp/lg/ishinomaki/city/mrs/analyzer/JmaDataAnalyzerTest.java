package jp.lg.ishinomaki.city.mrs.analyzer;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import jp.lg.ishinomaki.city.mrs.receiver.ReceiverConfig;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JmaDataAnalyzerTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ReceiverConfig.getInstance().loadYml("config/receiver.yml");
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
     * 2進数文字列をbyte[]に変換する（テスト用メソッド）
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
    public void ヘッディング部解析1() {

        // テスト用bch部(AN桁数19)
        byte[] bchSrc = asByteArray("0100010100000000000000001110010100001000010100000000000000000000001100000001001110110111100000100000000000000000000000000000000000000000000000000000000000000000");
        // テスト用tch部(19桁)
        byte[] tchSrc = "\n232320292334000000".getBytes();
        // テスト用本文部
        byte[] bodySrc = "test".getBytes();

        // メソッドの引数となるダミー電文作成
        byte[] src = new byte[bchSrc.length + tchSrc.length + bodySrc.length];
        System.arraycopy(bchSrc, 0, src, 0, bchSrc.length);
        System.arraycopy(tchSrc, 0, src, bchSrc.length, tchSrc.length);
        System.arraycopy(bodySrc, 0, src, bchSrc.length + tchSrc.length,
                bodySrc.length);

        // テスト対象クラス生成
        JmaDataAnalyzer target = new JmaDataAnalyzer();
        // BCHを設定
        target.bch = new BCH(src);
        
        // テスト対象メソッド実行
        target.analyzeHeading(src);

        // 一致する
        assertThat(target.heading, is(tchSrc));
    }

    @Test
    public void ヘッディング部解析2() {

        // テスト用bch部(AN桁数19)
        byte[] bchSrc = asByteArray("0100010100000000000000001110010100001000010100000000000000000000001100000001001110110111100000100000000000000000000000000000000000000000000000000000000000000000");
        // テスト用tch部(18桁)
        byte[] tchSrc = "\n23232029233400000".getBytes();
        // テスト用本文部
        byte[] bodySrc = "test".getBytes();

        // メソッドの引数となるダミー電文作成
        byte[] src = new byte[bchSrc.length + tchSrc.length + bodySrc.length];
        System.arraycopy(bchSrc, 0, src, 0, bchSrc.length);
        System.arraycopy(tchSrc, 0, src, bchSrc.length, tchSrc.length);
        System.arraycopy(bodySrc, 0, src, bchSrc.length + tchSrc.length,
                bodySrc.length);

        // テスト対象クラス生成
        JmaDataAnalyzer target = new JmaDataAnalyzer();
        // BCHを設定
        target.bch = new BCH(src);

        // テスト対象メソッド実行
        target.analyzeHeading(src);

        // 一致しない
        assertThat(target.heading, is(not(tchSrc)));
    }

    @Test
    public void ヘッディング部解析3TCHなしのケース() {

        // テスト用bch部(AN桁数0)
        byte[] bchSrc = asByteArray("0100010100000000000000001110010100001000010100000000000000000000001100000000000010110111100000100000000000000000000000000000000000000000000000000000000000000000");
        // テスト用tch部(0桁)
        byte[] tchSrc = "".getBytes();
        // テスト用本文部
        byte[] bodySrc = "test".getBytes();

        // メソッドの引数となるダミー電文作成
        byte[] src = new byte[bchSrc.length + tchSrc.length + bodySrc.length];
        System.arraycopy(bchSrc, 0, src, 0, bchSrc.length);
        System.arraycopy(tchSrc, 0, src, bchSrc.length, tchSrc.length);
        System.arraycopy(bodySrc, 0, src, bchSrc.length + tchSrc.length,
                bodySrc.length);

        // テスト対象クラス生成
        JmaDataAnalyzer target = new JmaDataAnalyzer();
        // BCHを設定
        target.bch = new BCH(src);

        // テスト対象メソッド実行
        target.analyzeHeading(src);

        // 一致する
        assertThat(target.heading, is(tchSrc));
    }

    @Test
    public void ボディ部解析1() {
        // テスト用bch部(AN桁数0,XMLタイプ=圧縮なしのXML)
        byte[] bchSrc = asByteArray("0100010100000000000000001110010100000100010100000000000000000000001100000000000010110111100000100000000000000000000000000000000000000000000000000000000000000000");
        // テスト用tch部(0桁)
        byte[] tchSrc = "".getBytes();
        // テスト用本文部
        byte[] bodySrc = "test".getBytes();

        // メソッドの引数となるダミー電文作成
        byte[] src = new byte[bchSrc.length + tchSrc.length + bodySrc.length];
        System.arraycopy(bchSrc, 0, src, 0, bchSrc.length);
        System.arraycopy(tchSrc, 0, src, bchSrc.length, tchSrc.length);
        System.arraycopy(bodySrc, 0, src, bchSrc.length + tchSrc.length,
                bodySrc.length);

        // テスト対象クラス生成
        JmaDataAnalyzer target = new JmaDataAnalyzer();
        // BCHを設定
        target.bch = new BCH(src);

        // テスト対象メソッド実行
        target.analyzeBody(src);

        // 一致する
        assertThat(target.contents, is(bodySrc));

    }
}
