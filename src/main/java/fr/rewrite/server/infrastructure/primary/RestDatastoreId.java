package fr.rewrite.server.infrastructure.primary;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.shared.error.domain.Assert;
import java.util.UUID;

public record RestDatastoreId(UUID uuid) {
  public RestDatastoreId {
    Assert.notNull("uuid", uuid);
  }
  public static RestDatastoreId fromDomain(DatastoreId datastoreId) {
    return new RestDatastoreId(datastoreId.uuid());
  }
  public DatastoreId toDomain() {
    return new DatastoreId(uuid());
  }
}
