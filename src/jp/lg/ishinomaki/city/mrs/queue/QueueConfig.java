package jp.lg.ishinomaki.city.mrs.queue;

public class QueueConfig {

    public static final String DOMAIN_SOCKET_DIR_KEY = "java.io.tmpdir";
    public static final String DOMAIN_SOCKET_FILE_FOR_PUSH = "junixsocket_push.sock";
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
