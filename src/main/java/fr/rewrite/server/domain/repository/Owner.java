package fr.rewrite.server.domain.repository;

import fr.rewrite.server.shared.error.domain.Assert;

public record Owner(String name) {
  public Owner {
    Assert.field("name", name).notBlank();
  }
  public String get() {
    return name;
  }
}
