package tool.dummyclient;


/**
 * JmaServerSocketControlからの各種通知を行うためのインターフェース
 */
public interface JmaClientSocketControlDelegate {
    
    /**
     * 受信データを通知
     * 
     * @param data 受信データ中のユーザデータ部
     */
    public void receiveData(byte[] data);
    
}
