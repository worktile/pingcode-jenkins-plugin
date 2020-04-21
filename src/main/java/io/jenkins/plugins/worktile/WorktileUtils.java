package io.jenkins.plugins.worktile;

import java.net.URL;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class WorktileUtils {

    public final static Logger logger = Logger.getLogger(WorktileUtils.class.getName());
    public final static Pattern WORKITEM_PATTERN = Pattern.compile("#[^/]*([A-Za-z0-9_])+-([0-9])+");

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

    public static long toSafeTs(long time) {
        return Math.round(time / 1000);
    }

    public static String[] getMatchSet(Pattern pattern, String[] messages, boolean breakFirstMatch, boolean origin) {
        HashSet<String> set = new HashSet<>();
        for (String msg : messages) {
            Matcher matcher = pattern.matcher(msg);
            if (matcher.find()) {
                if (origin) {
                    set.add(msg);
                } else {
                    set.add(matcher.group());
                }
                if (breakFirstMatch)
                    break;
            }
        }
        return set.toArray(new String[set.size()]);
    }

    public static String[] getWorkItems(String[] messages) {
        String[] workItems = WorktileUtils.getMatchSet(WorktileUtils.WORKITEM_PATTERN, messages, false, false);
        HashSet<String> sets = new HashSet<>();
        for (String item : workItems) {
            sets.add(item.substring(1));
        }
        return sets.toArray(new String[sets.size()]);
    }

    public static boolean isExpired(long future) {
        return toSafeTs(System.currentTimeMillis()) > future;
    }
}
