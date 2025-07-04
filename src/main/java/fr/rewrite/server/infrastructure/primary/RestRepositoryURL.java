package fr.rewrite.server.infrastructure.primary;

import fr.rewrite.server.domain.repository.RepositoryInvalidUrlException;
import fr.rewrite.server.shared.error.domain.Assert;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record RestRepositoryURL(String url) {
  public RestRepositoryURL {
    Assert.field("url", url).notBlank();
  }

  public static RestRepositoryURL from(String url) {
    return new RestRepositoryURL(url);
  }
}
