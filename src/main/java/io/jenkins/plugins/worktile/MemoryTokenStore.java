package io.jenkins.plugins.worktile;

import io.jenkins.plugins.worktile.model.WTTokenEntity;

import java.util.HashMap;
import java.util.Map;

public class MemoryTokenStore {

    protected static final Map<String, WTTokenEntity> store = new HashMap<>();

    public static boolean put(String clientId, String clientSecret, WTTokenEntity entity) {
        try {
            String key = WTHelper.md5(clientId + clientSecret);
            return store.put(key, entity) != null;
        }
        catch(Exception e) {
            return false;
        }
    }

    public static WTTokenEntity get(String clientId, String clientSecret) {
        try {
            String key = WTHelper.md5(clientId + clientSecret);
            return store.get(key);
        }
        catch(Exception exception) {
            return null;
        }
    }
}
