package fr.rewrite.server.domain.state;

import fr.rewrite.server.shared.error.domain.ErrorKey;

public enum StateErrorKey implements ErrorKey {
  STATE_NOT_FOUND("state-not-found");

  private final String key;

  StateErrorKey(String key) {
    this.key = key;
  }

  @Override
  public String get() {
    return key;
  }
}
