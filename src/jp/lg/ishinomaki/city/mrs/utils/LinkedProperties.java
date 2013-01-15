//
//  LinkedProperties.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

/**
 * プロパティファイルの内容をファイルの先頭に定義しているものから順序を保証して保持するユーティリティクラスです。
 * 
 */
public class LinkedProperties {

    private LinkedHashMap<String, String> properties = null;

    /**
     * コンストラクタです。 プロパティファイルの名前を指定してください。
     * 
     * @param fileName
     */
    public LinkedProperties(String fileName) {

        properties = new LinkedHashMap<String, String>();

        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.trim().length() == 0 || line.startsWith("#")) {
                    // 行のデータが"#"で始まっている場合はコメント行のため無視する
                    continue;
                }
                StringTokenizer st = new StringTokenizer(line, "=");
                String key = st.nextToken();
                String val = st.nextToken();

                properties.put(key.trim(), val.trim());
            }

            bufferedReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 順序が保証されたプロパティファイル内容をLinkedHashMapの形式で取得します。
     * 
     * @return LinkedHashMap プロパティファイル内容
     */
    public LinkedHashMap<String, String> getProperties() {
        return properties;
    }
}
