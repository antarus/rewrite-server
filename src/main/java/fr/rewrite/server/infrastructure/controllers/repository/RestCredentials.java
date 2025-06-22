package fr.rewrite.server.infrastructure.controllers.repository;

import fr.rewrite.server.domain.repository.Credentials;

public record RestCredentials(
  String username,
  // Personal Access Token
  String pat
) {
  public static Credentials toDomain(RestCredentials restCredentials) {
    if (restCredentials == null) {
      return null;
    }
    return new Credentials(restCredentials.username, restCredentials.pat);
  }
}
