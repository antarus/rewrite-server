package fr.rewrite.server.application;

import fr.rewrite.server.domain.datastore.DatastoreEventStore;
import fr.rewrite.server.domain.datastore.DatastoreNotFoundException;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentStateRepository;
import fr.rewrite.server.domain.repository.RepositoryExecution;
import fr.rewrite.server.domain.repository.command.RepositoryBranchCreate;
import fr.rewrite.server.domain.repository.command.RepositoryClone;
import fr.rewrite.server.domain.repository.command.RepositoryDelete;
import org.springframework.stereotype.Service;

@Service
public class RepositoryApplicationService {

  private final DatastoreCurrentStateRepository currentStateRepository;
  private final RepositoryExecution repositoryExecution;

  public RepositoryApplicationService(DatastoreCurrentStateRepository currentStateRepository, RepositoryExecution repositoryExecution) {
    this.currentStateRepository = currentStateRepository;
    this.repositoryExecution = repositoryExecution;
  }

  public void cloneRepository(RepositoryClone repositoryClone) {
    currentStateRepository
      .get(repositoryClone.datastoreId())
      .orElseThrow(() -> new DatastoreNotFoundException(repositoryClone.datastoreId()));

    repositoryExecution.executeClone(repositoryClone);
  }

  public void deleteRepository(RepositoryDelete repositoryDelete) {
    currentStateRepository
      .get(repositoryDelete.datastoreId())
      .orElseThrow(() -> new DatastoreNotFoundException(repositoryDelete.datastoreId()));

    repositoryExecution.deleteRepository(repositoryDelete);
  }

  public void createBranchAndCheckout(RepositoryBranchCreate repositoryBranchCreate) {
    currentStateRepository
      .get(repositoryBranchCreate.datastoreId())
      .orElseThrow(() -> new DatastoreNotFoundException(repositoryBranchCreate.datastoreId()));

    repositoryExecution.createBranchAndCheckout(repositoryBranchCreate);
  }
}
