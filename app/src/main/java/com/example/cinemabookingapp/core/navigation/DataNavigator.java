package com.example.cinemabookingapp.core.navigation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DataNavigator {

    private static volatile DataNavigator instance;
    private final Map<Integer, Object> dataTransferMap;
    private final AtomicInteger currentId;

    private DataNavigator() {
        dataTransferMap = new HashMap<>();
        currentId = new AtomicInteger(0);
    }

    public static DataNavigator getInstance() {
        if (instance == null) {
            synchronized (DataNavigator.class) {
                if (instance == null) {
                    instance = new DataNavigator();
                }
            }
        }
        return instance;
    }

    /**
     * Stores data and returns an auto-incremented integer key.
     */
    public synchronized int pushData(Object data) {
        int key = currentId.incrementAndGet();
        dataTransferMap.put(key, data);
        return key;
    }

    /**
     * Retrieves data and removes it to prevent memory leaks.
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T popData(int key) {
        if (dataTransferMap.containsKey(key)) {
            return (T) dataTransferMap.remove(key);
        }
        return null;
    }
}