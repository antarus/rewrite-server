package fr.rewrite.server.domain.repository.command;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.shared.error.domain.Assert;
import org.jmolecules.architecture.cqrs.Command;

@Command
public record RepositoryDelete(DatastoreId datastoreId) {
  public RepositoryDelete {
    Assert.notNull("datastoreId", datastoreId);
  }

  public static RepositoryDelete from(DatastoreId datastoreId) {
    return new RepositoryDelete(datastoreId);
  }
}
