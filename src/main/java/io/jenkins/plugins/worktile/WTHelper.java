package io.jenkins.plugins.worktile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.EnvVars;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WTHelper {

    public static final Logger logger = Logger.getLogger(WTHelper.class.getName());

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

    public static String apiV1(String endpoint) {
        return endpoint + "/v1";
    }

    public static String prettyJSON(Object object) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }

    public static String md5(String source) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("MD5");
        mDigest.update(source.getBytes());
        return new BigInteger(1, mDigest.digest()).toString(16);
    }

    public static String statusOfRun(final Run<?, ?> run) {
        Result result = run.getResult();
        return result == null ? "success" : result.toString().toLowerCase();
    }

    public static EnvVars safeEnvVars(Run<?, ?> run) {
        try {
            return run.getEnvironment(TaskListener.NULL);
        } catch (Exception e) {
            return new EnvVars();
        }
    }

    public static long toSafeTs(long time) {
        return Math.round(Math.floorDiv(time, 1000));
    }

    public static String resolveOverview(Run<?, ?> run, String overviewPattern, String defaultSummary) {
        if (overviewPattern == null || overviewPattern.equals("")) {
            return null;
        }
        Pattern pattern = Pattern.compile(overviewPattern);
        try {
            List<String> logs = run.getLog(999);
            for (String log : logs) {
                Matcher matcher = pattern.matcher(log);
                if (matcher.find()) {
                    return log;
                }
            }
            if (defaultSummary != null && !defaultSummary.equals("")) {
                return defaultSummary;
            }
            return null;
        } catch (Exception exception) {
            if (defaultSummary != null && !defaultSummary.equals("")) {
                return defaultSummary;
            }
            return null;
        }
    }

    public static List<String> formatWorkItems(List<String> workItems) {
        HashSet<String> set = new HashSet<>();
        for (String item : workItems) {
            set.add(item.substring(1));
        }
        return new ArrayList<>(set);
    }
}
