package fr.rewrite.server.domain.repository;

import fr.rewrite.server.shared.error.domain.Assert;

public record BranchName(String value) {
  public BranchName {
    Assert.field("value", value).notBlank();
  }
  public String get() {
    return value;
  }
}
