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

}
