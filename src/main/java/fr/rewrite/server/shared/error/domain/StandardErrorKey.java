package fr.rewrite.server.shared.error.domain;

public enum StandardErrorKey implements ErrorKey {
  BAD_REQUEST("bad-request"),
  CONFLICT("conflict"),
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
