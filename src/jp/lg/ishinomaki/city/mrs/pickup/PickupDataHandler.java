//
//  PickupDataHandler.java
//  LGDIS-Input-Tools
//
//  Copyright (C) 2012 ISHINOMAKI CITY OFFICE.
//
//

package jp.lg.ishinomaki.city.mrs.pickup;

/**
 * キューから取得したデータに対して具体的な処理を行うインターフェース定義です
 * 
 */
public interface PickupDataHandler {

    /**
     * 処理を行います。
     * 
     * @param byte[] 本文データ
     */
    public void handle(byte[] data);
}
