package fr.rewrite.server.domain.updater;

import fr.rewrite.server.shared.error.domain.Assert;

public record Recipe(String name) {
  public Recipe {
    Assert.field("name", name).notBlank();
  }
}
