package fr.rewrite.server.domain.datastore.command;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.error.domain.Assert;
import org.jmolecules.architecture.cqrs.Command;

@Command
public record DatastoreCreation(RepositoryURL repositoryURL) {
  public DatastoreCreation {
    Assert.notNull("repositoryURL", repositoryURL);
  }
  public DatastoreId datastoreId() {
    return DatastoreId.from(repositoryURL);
  }
}
