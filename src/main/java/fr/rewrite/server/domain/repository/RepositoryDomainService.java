package fr.rewrite.server.domain.repository;

import fr.rewrite.server.domain.EventPublisher;
import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.StatusEnum;
import fr.rewrite.server.domain.datastore.DatastoreEventStore;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.datastore.DatastoreNotFoundException;
import fr.rewrite.server.domain.datastore.InvalidStateTransitionException;
import fr.rewrite.server.domain.datastore.event.DatastoreEvent;
import fr.rewrite.server.domain.datastore.event.DatastoreStatusChanged;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentState;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentStateRepository;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.repository.command.RepositoryBranchCreate;
import fr.rewrite.server.domain.repository.command.RepositoryClone;
import fr.rewrite.server.domain.repository.command.RepositoryDelete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DomainService
public class RepositoryDomainService {

  private static final Logger log = LoggerFactory.getLogger(RepositoryDomainService.class);
  private final RepositoryPort repositoryPort;
  private final DatastoreEventStore eventStore;
  private final EventPublisher<DatastoreEvent> eventPublisher;
  private final DatastoreCurrentStateRepository datastoreCurrentStateRepository;

  public RepositoryDomainService(
    RepositoryPort repositoryPort,
    DatastoreEventStore eventStore,
    EventPublisher<DatastoreEvent> eventPublisher,
    DatastoreCurrentStateRepository datastoreCurrentStateRepository
  ) {
    this.repositoryPort = repositoryPort;
    this.eventStore = eventStore;
    this.eventPublisher = eventPublisher;
    this.datastoreCurrentStateRepository = datastoreCurrentStateRepository;
  }

  public void cloneRepository(RepositoryClone cmd) {
    transitionToState(cmd.datastoreId(), StatusEnum.REPOSITORY_CLONING);
    try {
      repositoryPort.cloneRepository(cmd.datastoreId(), cmd.repositoryURL(), cmd.credential());

      transitionToState(cmd.datastoreId(), StatusEnum.REPOSITORY_CLONED);
    } catch (Exception e) {
      transitionToState(cmd.datastoreId(), StatusEnum.REPOSITORY_CLONE_FAILED);
      throw e;
    }
  }

  public void deleteRepository(RepositoryDelete cmd) {
    transitionToState(cmd.datastoreId(), StatusEnum.REPOSITORY_DELETING);
    try {
      repositoryPort.deleteRepository(cmd.datastoreId());
      transitionToState(cmd.datastoreId(), StatusEnum.REPOSITORY_DELETED);
    } catch (Exception e) {
      transitionToState(cmd.datastoreId(), StatusEnum.REPOSITORY_DELETE_FAILED);
      throw e;
    }
  }

  public void createBranchAndCheckout(RepositoryBranchCreate cmd) {
    transitionToState(cmd.datastoreId(), StatusEnum.BRANCH_CREATING);
    try {
      repositoryPort.createBranchAndCheckout(cmd.datastoreId(), cmd.branchName());
      transitionToState(cmd.datastoreId(), StatusEnum.BRANCH_CREATED);
    } catch (Exception e) {
      transitionToState(cmd.datastoreId(), StatusEnum.BRANCH_CREATION_FAILED);
      throw e;
    }
  }

  private void transitionToState(DatastoreId datastoreId, StatusEnum nextStatus) {
    DatastoreCurrentState currentState = datastoreCurrentStateRepository
      .get(datastoreId)
      .orElseThrow(() -> new DatastoreNotFoundException(datastoreId));

    log.trace("transitionToState datastoreId: {} from: {} to {}", datastoreId, currentState.lastStatus(), nextStatus);
    if (!currentState.lastStatus().canTransitionTo(nextStatus)) {
      throw new InvalidStateTransitionException(datastoreId, currentState.lastStatus(), nextStatus);
    }
    SequenceId nextNumber = eventStore.nextSequenceId(datastoreId);
    log.debug("transitionToState datastoreId: {} nextNumber: {}", datastoreId, nextNumber);
    DatastoreStatusChanged event = DatastoreStatusChanged.from(datastoreId, nextStatus, nextNumber);
    eventStore.save(event);
    eventPublisher.publish(event);
  }
}
