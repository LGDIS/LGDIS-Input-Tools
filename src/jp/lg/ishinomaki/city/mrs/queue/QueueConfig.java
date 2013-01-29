//
//  QueueConfig.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.queue;

/**
 * キュー管理機能用の各種設定
 */
public class QueueConfig {

    /**
     * ソケットファイル作成ディレクトリを取得する際のキーとなる文字列
     */
    public static final String DOMAIN_SOCKET_DIR_KEY = "java.io.tmpdir";

    /**
     * push用ソケットファイル名
     */
    public static final String DOMAIN_SOCKET_FILE_FOR_PUSH = "junixsocket_push.sock";

    /**
     * pop用ソケットファイル名
     */
    public static final String DOMAIN_SOCKET_FILE_FOR_POP = "junixsocket_pop.sock";

    /**
     * キュー可能なMAXサイズ指定 基本的にはキューアイテムをすぐに取り出されるため10000あれば十分
     */
    public static final int QUEUE_MAX_SIZE = 10000;

    /**
     * キューイング可能なデータサイズはintの最大値
     */
    public static final int DATA_MAX_SIZE = Integer.MAX_VALUE;
}
