package fr.rewrite.server.domain.repository.command;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.repository.Credentials;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.error.domain.Assert;
import java.util.Optional;
import org.jmolecules.architecture.cqrs.Command;

@Command
public record RepositoryClone(DatastoreId datastoreId, RepositoryURL repositoryURL, Optional<Credentials> credential) {
  public RepositoryClone {
    Assert.notNull("datastoreId", datastoreId);
    Assert.notNull("repositoryURL", repositoryURL);
    Assert.notNull("credential", credential);
  }

  public static RepositoryClone from(DatastoreId datastoreId, RepositoryURL repositoryURL) {
    return new RepositoryClone(datastoreId, repositoryURL, Optional.empty());
  }
  public static RepositoryClone from(DatastoreId datastoreId, RepositoryURL repositoryURL, Credentials credential) {
    if (credential == null) {
      return RepositoryClone.from(datastoreId, repositoryURL);
    }
    return new RepositoryClone(datastoreId, repositoryURL, Optional.of(credential));
  }
}
