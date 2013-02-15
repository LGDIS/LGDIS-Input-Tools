package jp.lg.ishinomaki.city.mrs.queue;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

/**
 * キューを管理するインスタンス
 * 
 */
public class QueueManager {

    // ログ用
    private Logger log = Logger.getLogger(QueueManager.class.getSimpleName());

    /**
     * シングルトンインスタンス変数
     */
    private static QueueManager instance;

    /**
     * 実際のキューイング用インスタンス
     */
    private ArrayBlockingQueue<byte[]> queue;

    /**
     * デフォルトコンストラクタを外部から実行できないようにする.
     * 
     */
    private QueueManager() {
        QueueConfig config = QueueConfig.getInstance();
        queue = new ArrayBlockingQueue<byte[]>(config.getQueueMaxSize());
    }

    /**
     * シングルトンインスタンス取得メソッド
     * 
     * @return MessageQueue インスタンス
     */
    public static QueueManager getInstance() {
        if (instance == null) {
            instance = new QueueManager();
        }
        return instance;
    }

    /**
     * キューの末尾にメッセージを挿入します。<br>
     * キューがいっぱいの場合は利用可能になるまで待機します。<br>
     * 引数がnullの場合はNullPointerExceptionを発行せず、処理を中断します。
     * 
     * @param byte[]
     *        キューに追加するメッセージ
     * @throws InterruptedException
     *             待機中に割り込みが発生した場合
     */
    public void push(byte[] data) throws InterruptedException, IOException {
        queue.offer(data);
        log.info("キューにデータを登録しました。 現在キューイングされているデータ数:" + queue.size());
    }

    /**
     * キューの先頭からメッセージを取得します。取得したメッセージはキューから削除します。<br>
     * キューが空の場合はキューにメッセージが挿入されるまで待機します。<br>
     * 
     * @return byte[] キューから取得したデータ
     * @throws InterruptedException
     *             待機中に割り込みが発生した場合
     */
    public byte[] pop() throws InterruptedException {
        byte[] data = queue.take();
        log.info("キューからデータを取得しました。 現在キューイングされているデータ数:" + queue.size());
        return data;
    }

}
