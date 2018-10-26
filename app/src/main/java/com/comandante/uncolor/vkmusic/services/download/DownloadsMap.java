package com.comandante.uncolor.vkmusic.services.download;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Uncolor on 26.10.2018.
 */

class DownloadsMap<K,V> extends HashMap<K, V> {

    Map<V,K> reverseMap = new HashMap<>();

    @Override
    public V put(K key, V value) {
        reverseMap.put(value, key);
        return super.put(key, value);
    }

    public K getKey(V value){
        return reverseMap.get(value);
    }
}