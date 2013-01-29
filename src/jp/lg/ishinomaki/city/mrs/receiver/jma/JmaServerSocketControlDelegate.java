//
//  JmaServerSocketControlDelegate.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.receiver.jma;

import java.net.Socket;

/**
 * JmaServerSocketControlからの各種通知を行うためのインターフェース
 */
public interface JmaServerSocketControlDelegate {
    
    /**
     * 接続開始を通知
     * 
     * @param socket 接続時のSocketオブジェクト
     */
    public void acceptConnection(Socket socket);
    
    /**
     * 受信データを通知
     * 
     * @param data 受信データ中のユーザデータ部
     */
    public void receiveData(String type, byte[] data);
    
}
