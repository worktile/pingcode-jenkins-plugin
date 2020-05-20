package io.jenkins.plugins.worktile.model;

import io.jenkins.plugins.worktile.WTHelper;

public class WTTokenEntity {
  public String accessToken;
  public String tokenType;
  public long expiresIn;

  public boolean isExpired() {
    return expiresIn < WTHelper.toSafeTs(System.currentTimeMillis());
  }
}
