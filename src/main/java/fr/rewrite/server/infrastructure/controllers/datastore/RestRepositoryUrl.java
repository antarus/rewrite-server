package fr.rewrite.server.infrastructure.controllers.datastore;

import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.error.domain.Assert;

public record RestRepositoryUrl(String url) {
  public RestRepositoryUrl {
    Assert.field("url", url).notBlank();
  }

  public static RepositoryURL toDomain(RestRepositoryUrl restRepositoryUrl) {
    return new RepositoryURL(restRepositoryUrl.url());
  }
}
