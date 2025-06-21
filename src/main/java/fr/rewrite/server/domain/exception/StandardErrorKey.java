package fr.rewrite.server.domain.exception;

public enum StandardErrorKey implements ErrorKey {
  BAD_REQUEST("bad-request"),
  INTERNAL_SERVER_ERROR("internal-server-error");

  private final String key;

  StandardErrorKey(String key) {
    this.key = key;
  }

  @Override
  public String get() {
    return key;
  }
}
