package fr.rewrite.server.domain.repository.command;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.repository.RepositoryBranchName;
import fr.rewrite.server.shared.error.domain.Assert;
import org.jmolecules.architecture.cqrs.Command;

@Command
public record RepositoryBranchCreate(DatastoreId datastoreId, RepositoryBranchName branchName) {
  public RepositoryBranchCreate {
    Assert.notNull("datastoreId", datastoreId);
    Assert.notNull("branchName", branchName);
  }

  public static RepositoryBranchCreate from(DatastoreId datastoreId, RepositoryBranchName branchName) {
    return new RepositoryBranchCreate(datastoreId, branchName);
  }
}
