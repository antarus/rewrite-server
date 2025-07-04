package fr.rewrite.server.domain.repository;

import fr.rewrite.server.domain.datastore.DatastoreId;
import java.util.Optional;

public interface RepositoryPort {
  void cloneRepository(DatastoreId datastoreId, RepositoryURL repositoryURL, Optional<Credentials> credentials)
    throws CloneRepositoryException;
  void deleteRepository(DatastoreId datastoreId);
  void createBranchAndCheckout(DatastoreId datastoreId, RepositoryBranchName repositoryBranchName) throws CreateBranchException;
}
