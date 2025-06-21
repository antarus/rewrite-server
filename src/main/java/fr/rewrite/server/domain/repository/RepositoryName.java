package fr.rewrite.server.domain.repository;

import fr.rewrite.server.shared.error.domain.Assert;

public record RepositoryName(String name) {
  public RepositoryName {
    Assert.field("name", name).notBlank();
  }
  public String get() {
    return name;
  }
}
