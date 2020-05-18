package io.jenkins.plugins.worktile.model;

import java.util.regex.Pattern;

public final class WTItemPattern {
  public final Pattern branchPattern = Pattern.compile("#[^/]*([A-Za-z0-9_])+-([0-9])+");
  public final Pattern messagePattern = Pattern.compile("regex");
}
