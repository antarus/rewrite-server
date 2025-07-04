package fr.rewrite.server.domain.repository;

import fr.rewrite.server.shared.error.domain.ErrorKey;

public enum RepositoryErrorKey implements ErrorKey {
  REPOSITORY_INVALID_URI("repository-invalid-uri"),
  REPOSITORY_CLONE_ERROR("repository-clone-error"),
  BRANCH_CREATION_ERROR("branch-creation-error");

  private final String key;

  RepositoryErrorKey(String key) {
    this.key = key;
  }

  @Override
  public String get() {
    return key;
  }
}
