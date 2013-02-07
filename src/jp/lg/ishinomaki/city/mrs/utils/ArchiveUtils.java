//
//  ArchiveUtils.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

/**
 * アーカイブファイルの解凍・圧縮用メソッド集
 * 
 */
public class ArchiveUtils {

    /**
     * gzip形式のバイト配列を解凍しバイト配列形式で返却
     * 
     * @param raw
     * @return
     */
    public static byte[] ungzip(byte[] raw) {
        byte[] result = null;
        try {
            InputStream is = new BufferedInputStream(new GZIPInputStream(
                    new ByteArrayInputStream(raw)));
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            OutputStream os = new BufferedOutputStream(ba);
            int c;
            while ((c = is.read()) != -1) {
                os.write(c);
            }
            os.flush();

            result = ba.toByteArray();

            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * zip形式のバイト配列を解凍しバイト配列形式で返却
     * 
     * @param raw
     * @return
     */
    public static byte[] unzip(byte[] raw) {
        byte[] result = null;
        try {
            InputStream is = new BufferedInputStream(new ZipInputStream(
                    new ByteArrayInputStream(raw)));
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            OutputStream os = new BufferedOutputStream(ba);
            int c;
            while ((c = is.read()) != -1) {
                os.write(c);
            }
            os.flush();

            result = ba.toByteArray();

            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * tar形式のバイト配列を解凍しkey:ファイル名 data:ファイルデータの形式のMap形式で返却
     * 
     * @param data
     * @return Map<String, Object>
     */
    public static List<Map<String, Object>> untar(byte[] data) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        TarInputStream tis = null;
        try {
            // Tarファイルを読み込み
            tis = new TarInputStream(new ByteArrayInputStream(data));

            // Tar内のファイルを1つづつ処理
            TarEntry entry = tis.getNextEntry();
            while (entry != null) {
                // ファイル情報を格納するMap
                Map<String, Object> fileMap = new HashMap<String, Object>();

                // ファイル名を取得しMapに格納
                String fileName = entry.getName();
                fileMap.put("name", fileName);

                // サイズを取得
                int size = (int) entry.getSize();
                // ファイル内容取得
                ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
                tis.copyEntryContents(bos);
                byte[] fileData = bos.toByteArray();
                fileMap.put("contents", fileData);

                // 戻りテーブルに設定
                result.add(fileMap);

                // 次のファイルを取得
                entry = tis.getNextEntry();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                tis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}