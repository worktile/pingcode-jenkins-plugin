package io.jenkins.plugins.worktile;

import hudson.EnvVars;
import hudson.model.Result;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import io.jenkins.plugins.worktile.model.WTItemPattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
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

  public static String md5(String source) throws NoSuchAlgorithmException {
    MessageDigest mDigest = MessageDigest.getInstance("MD5");
    mDigest.update(source.getBytes());
    return new BigInteger(1, mDigest.digest()).toString(16);
  }

  public static String statusOfRun(final Run<?, ?> run) {
    Result result = run.getResult();
    return result == null ? "failure" : result.toString().toLowerCase();
  }

  public static boolean isExpired(long future) {
    return toSafeTs(System.currentTimeMillis()) > future;
  }

  public static long toSafeTs(long time) {
    return Math.round(Math.floorDiv(time, 1000));
  }

  public static String renderStringByEnvVars(String template, EnvVars vars) {
    HashMap<String, String> map = new HashMap<>();
    vars.forEach(map::put);
    StringSubstitutor sub = new StringSubstitutor(map);
    return sub.replace(template);
  }

  public static String resolveOverview(Run<?, ?> run, String overviewPattern) {
    if (overviewPattern == null) {
      return null;
    }
    try {
      Pattern pattern = Pattern.compile(overviewPattern);
      List<String> matched = WTHelper.getMatchSet(pattern, run.getLog(999), true, true);
      return matched.size() > 0 ? matched.get(0) : null;
    } catch (Exception exception) {
      return null;
    }
  }

  public static List<String> getMatchSet(
      Pattern pattern, List<String> messages, boolean breakFirstMatch, boolean origin) {
    HashSet<String> set = new HashSet<>();
    for (String msg : messages) {
      if (msg != null) {
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
          if (origin) {
            set.add(msg);
          } else {
            set.add(matcher.group());
          }
          if (breakFirstMatch) {
            break;
          }
        }
      }
    }
    return new ArrayList<>(set);
  }

  public static List<String> extractWorkItems(
      List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeLogSets, EnvVars vars) {
    List<String> array = new ArrayList<>();
    changeLogSets.forEach(
        changeLogSet -> {
          for (Object change : changeLogSet) {
            ChangeLogSet.Entry entry = (ChangeLogSet.Entry) change;
            String scmMessage = entry.getMsg();
            logger.info("Scm message = " + scmMessage);
            array.add(scmMessage);
          }
        });
    String branch = vars.get("GIT_BRANCH");
    if (branch != null) {
      array.add(branch);
    }
    return getWorkItems(array);
  }

  public static List<String> getWorkItems(List<String> messages) {
    List<String> workItems =
        WTHelper.getMatchSet(WTItemPattern.branchPattern, messages, false, false);
    HashSet<String> set = new HashSet<>();
    for (String item : workItems) {
      set.add(item.substring(1));
    }
    return new ArrayList<>(set);
  }
}
