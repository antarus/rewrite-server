package fr.rewrite.server.infrastructure.controllers.datastore;

import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.shared.error.domain.Assert;
import java.util.UUID;

public record RestDatastore(UUID rewriteId) {
  public RestDatastore {
    Assert.notNull("rewriteId", rewriteId);
  }
  public static RestDatastore from(Datastore datastore) {
    return new RestDatastore(datastore.rewriteId().uuid());
  }
}
