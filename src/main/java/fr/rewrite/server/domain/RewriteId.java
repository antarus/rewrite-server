package fr.rewrite.server.domain;

import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.error.domain.Assert;
import java.util.UUID;

public record RewriteId(UUID uuid) {
  public RewriteId {
    Assert.notNull("uuid", uuid);
  }
  public static RewriteId from(RepositoryURL repositoryURL) {
    Assert.notNull("repositoryURL", repositoryURL);
    return new RewriteId(UUID.nameUUIDFromBytes(repositoryURL.url().getBytes()));
  }
  public static RewriteId from(UUID uuid) {
    return new RewriteId(uuid);
  }
  public UUID get() {
    return uuid();
  }
}
