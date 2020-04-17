package io.jenkins.plugins.worktile;

import java.net.URL;
import org.apache.commons.lang3.StringUtils;

public class WorktileUtils {

    public WorktileUtils() {
    }

    public static boolean isURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isNotBlank(String value) {
        return StringUtils.isNotBlank(value);
    }

    public static boolean isBlank(String value) {
        return StringUtils.isBlank(value);
    }
}
