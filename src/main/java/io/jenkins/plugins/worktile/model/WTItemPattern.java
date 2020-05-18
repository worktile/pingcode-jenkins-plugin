package io.jenkins.plugins.worktile.model;

import java.util.regex.Pattern;

public final class WTItemPattern {
  public static final Pattern branchPattern = Pattern.compile("#[^/]*([A-Za-z0-9_])+-([0-9])+");
}
