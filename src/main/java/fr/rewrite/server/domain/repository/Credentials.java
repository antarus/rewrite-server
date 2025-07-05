package fr.rewrite.server.domain.repository;

import fr.rewrite.server.shared.error.domain.Assert;

public record Credentials(
  String username,
  // Personal Access Token
  String pat
) {
  public Credentials {
    Assert.field("username", username).notBlank();
    Assert.field("pat", pat).notNull();
  }

  @Override
  public String toString() {
    return "Credentials{" + "username='" + username + '\'' + ", pat='[ *** Mask ***]'" + '}';
  }
}
