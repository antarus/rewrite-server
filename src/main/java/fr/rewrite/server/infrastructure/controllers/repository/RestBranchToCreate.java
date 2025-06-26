package fr.rewrite.server.infrastructure.controllers.repository;

import fr.rewrite.server.shared.error.domain.Assert;

public record RestBranchToCreate(String name) {
  public RestBranchToCreate {
    Assert.notNull("name", name);
  }
}
