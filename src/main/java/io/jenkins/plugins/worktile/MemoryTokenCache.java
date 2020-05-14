package io.jenkins.plugins.worktile;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.jenkins.plugins.worktile.model.WTTokenEntity;

public class MemoryTokenCache {

    public static final Logger logger = Logger.getLogger(MemoryTokenCache.class.getName());

    public static final Map<String, WTTokenEntity> store = new HashMap<>();

    public static boolean put(String clientId, String clientSecret, WTTokenEntity entity) {
        try {
            String key = WTHelper.md5(clientId + clientSecret);
            return store.put(key, entity) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static WTTokenEntity get(String clientId, String clientSecret) {
        try {
            String key = WTHelper.md5(clientId + clientSecret);
            logger.info("get wtTokenEntity by " + key);
            WTTokenEntity token = store.get(key);
            return token;
        } catch (Exception exception) {
            logger.info("get wtTokenEntity error" + exception.getMessage());
            return null;
        }
    }
}
