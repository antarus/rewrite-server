package fr.rewrite.server.domain;

import fr.rewrite.server.shared.error.domain.Assert;
import java.util.UUID;

public record RewriteId(UUID uuid) {
  public RewriteId {
    Assert.notNull("uuid", uuid);
  }
  public static RewriteId fromString(String s) {
    Assert.field("s", s).notBlank();
    return new RewriteId(UUID.nameUUIDFromBytes(s.getBytes()));
  }
  public UUID get() {
    return uuid();
  }
}
