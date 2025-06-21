package fr.rewrite.server.domain.repository;

import fr.rewrite.server.shared.error.domain.Assert;
import java.util.UUID;

public record RepositoryId(UUID uuid) {
  public RepositoryId {
    Assert.notNull("uuid", uuid);
  }
  public static RepositoryId from(String s) {
    Assert.field("s", s).notBlank();
    return new RepositoryId(UUID.nameUUIDFromBytes(s.getBytes()));
  }
  public UUID get() {
    return uuid;
  }
}
