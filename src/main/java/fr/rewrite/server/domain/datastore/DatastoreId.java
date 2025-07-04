package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.error.domain.Assert;
import java.util.UUID;

public record DatastoreId(UUID uuid) {
  public DatastoreId {
    Assert.notNull("uuid", uuid);
  }
  public static DatastoreId from(RepositoryURL repositoryURL) {
    Assert.notNull("repositoryURL", repositoryURL);
    return new DatastoreId(UUID.nameUUIDFromBytes(repositoryURL.url().getBytes()));
  }
  public static DatastoreId from(UUID uuid) {
    return new DatastoreId(uuid);
  }
  public UUID get() {
    return uuid();
  }
}
