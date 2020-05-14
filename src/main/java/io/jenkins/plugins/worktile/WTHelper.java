package io.jenkins.plugins.worktile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import hudson.EnvVars;
import hudson.XmlFile;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogSet;
import jenkins.model.Jenkins;

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

    public static String md5(String source) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("MD5");
        mDigest.update(source.getBytes());
        return new BigInteger(1, mDigest.digest()).toString(16);
    }

    public static List<String> getMatchSet(Pattern pattern, List<String> messages, boolean breakFirstMatch,
            boolean origin) {
        HashSet<String> set = new HashSet<>();
        for (String msg : messages) {
            if (msg != null) {
                Matcher matcher = pattern.matcher(msg);
                if (matcher != null && matcher.find()) {
                    if (origin) {
                        set.add(msg);
                    } else {
                        set.add(matcher.group());
                    }
                    if (breakFirstMatch)
                        break;
                }
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

    public static String renderStringByEnvVars(String template, EnvVars vars) {
        HashMap<String, String> map = new HashMap<>();
        vars.forEach((key, value) -> {
            map.put(key, value);
        });
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

    public static List<String> resolveWorkItemsFromPipelineStep(WorkflowRun run) {
        List<String> array = new ArrayList<>();
        List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeLogSets = run.getChangeSets();
        changeLogSets.forEach(changeLogSet -> {
            for (Object change : changeLogSet) {
                ChangeLogSet.Entry entry = (ChangeLogSet.Entry) change;
                array.add(entry.getMsg());
            }
        });
        try {
            String branch = run.getEnvironment(TaskListener.NULL).get("GIT_BRANCH");
            logger.info("Branch from env" + branch);
            array.add(branch);
        } catch (Exception error) {
            logger.info("Get $GIT_BRANCH error");
        }

        return getWorkItems(array);
    }
}
