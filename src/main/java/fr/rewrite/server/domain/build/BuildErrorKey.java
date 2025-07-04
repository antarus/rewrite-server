package fr.rewrite.server.domain.build;

import fr.rewrite.server.shared.error.domain.ErrorKey;

public enum BuildErrorKey implements ErrorKey {
  BUILD_OPERATION_ERROR("build-operation-error"),
  BUILD_PROJECT_FAILED("build-project-failed"),
  BUILD_CLASSPATH_FAILED("build-classpath-failed"),
  ;

  private final String key;

  BuildErrorKey(String key) {
    this.key = key;
  }

  @Override
  public String get() {
    return key;
  }
}
