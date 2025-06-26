package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.error.domain.Assert;

public record Datastore(RewriteId rewriteId) {
  public Datastore {
    Assert.notNull("rewriteId", rewriteId);
  }
  public static Datastore from(RepositoryURL repositoryURL) {
    return new Datastore(RewriteId.from(repositoryURL));
  }
  public static Datastore from(RewriteId rewriteId) {
    return new Datastore(rewriteId);
  }
}
