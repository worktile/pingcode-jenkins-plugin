package io.jenkins.plugins.worktile;

import hudson.XmlFile;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WTHelper {

    public final static Logger logger = Logger.getLogger(WTHelper.class.getName());
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

    public static String apiV1(String endpoint) {
        return endpoint + "/v1";
    }

    public static List<String> getMatchSet(Pattern pattern, List<String> messages, boolean breakFirstMatch,
            boolean origin) {
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
        return new ArrayList<>(set);
    }

    public static List<String> getWorkItems(List<String> messages) {
        List<String> workItems = WTHelper.getMatchSet(WTHelper.WORKITEM_PATTERN, messages, false, false);
        HashSet<String> set = new HashSet<>();
        for (String item : workItems) {
            set.add(item.substring(1));
        }
        return new ArrayList<>(set);
    }

    public static boolean isExpired(long future) {
        return toSafeTs(System.currentTimeMillis()) > future;
    }

    public static XmlFile getTokenXmlFile() {
        File file = new File(Objects.requireNonNull(Jenkins.getInstanceOrNull()).getRootDir(), "worktile.token.xml");
        return new XmlFile(file);
    }

    public static void RemoveTokenFile() {
        File file = new File(Objects.requireNonNull(Jenkins.getInstanceOrNull()).getRootDir(), "worktile.token.xml");
        file.delete();
    }

    // @SuppressWarnings("rawtypes")
    // public static HashMap<String, String> changeLogInBuild(AbstractBuild build) {
    // HashMap<String, String> map = new HashMap<>();
    // ChangeLogSet changes = build.getChangeSet();
    // for (Object change : changes) {
    // ChangeLogSet.Entry entry = (ChangeLogSet.Entry) change;
    // map.put("SCM_DISPLAY_NAME", entry.getAuthor().getDisplayName());
    // map.put("SCM_FULL_NAME", entry.getAuthor().getFullName());
    // map.put("SCM_COMMIT_MSG", entry.getMsg());
    // map.put("SCM_COMMIT_ID", entry.getCommitId());
    // map.put("SCM_COMMITTED_AT", String.valueOf(entry.getTimestamp()));
    // }
    // return map;
    // }

    @SuppressWarnings("rawtypes")
    public static HashMap<String, String> predefined(AbstractBuild build) throws IOException, InterruptedException {
        HashMap<String, String> map = new HashMap<>();
        build.getEnvironment(TaskListener.NULL).forEach((key, value) -> {
            map.put(key, value);
        });
        return map;
    }

    @SuppressWarnings("rawtypes")
    public static String renderTemplateString(String template, AbstractBuild build) {
        HashMap<String, String> map = new HashMap<>();
        try {
            map = predefined(build);
        } catch (Exception error) {
        }
        StringSubstitutor sub = new StringSubstitutor(map);
        return sub.replace(template);
    }

    @SuppressWarnings("rawtypes")
    public static List<String> resolveSCMMessage(AbstractBuild build) {
        List<String> array = new ArrayList<>();
        ChangeLogSet changes = build.getChangeSet();
        for (Object change : changes) {
            ChangeLogSet.Entry entry = (ChangeLogSet.Entry) change;
            array.add(entry.getMsg());
        }
        return array;
    }

    @SuppressWarnings("rawtypes")
    public static List<String> resolveSCMBranch(AbstractBuild build, TaskListener listener) {
        List<String> array = new ArrayList<>();
        try {
            String branch = build.getEnvironment(listener).get("GIT_BRANCH");
            array.add(branch);
        } catch (Exception error) {
            logger.info("Get $GIT_BRANCH error");
        }
        return array;
    }

    @SuppressWarnings("rawtypes")
    public static List<String> resolveWorkItems(AbstractBuild build, TaskListener listener) {
        List<String> messages = resolveSCMMessage(build);
        List<String> branches = resolveSCMBranch(build, listener);
        messages.addAll(branches);
        return getWorkItems(messages);
    }
}
