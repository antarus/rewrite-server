package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.error.domain.Assert;
import java.nio.file.Path;
import java.util.Set;

public record Datastore(RewriteId rewriteId, Path path, Set<Path> files) {
  public Datastore {
    Assert.notNull("rewriteId", rewriteId);
    Assert.notNull("path", path);
    Assert.field("files", files).noNullElement();
  }
  public static Datastore from(RepositoryURL repositoryURL, Path path, Set<Path> files) {
    return new Datastore(RewriteId.from(repositoryURL), path, files);
  }
  public static Datastore from(RewriteId rewriteId, Path path, Set<Path> files) {
    return new Datastore(rewriteId, path, files);
  }
}
