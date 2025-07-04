package fr.rewrite.server.application;

import fr.rewrite.server.domain.datastore.*;
import fr.rewrite.server.domain.datastore.command.DatastoreCreation;
import fr.rewrite.server.domain.datastore.event.DatastoreHistory;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentState;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentStateRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatastoreApplicationService {

  private final DatastoreCurrentStateRepository currentStateRepository;
  private final DatastoreEventStore eventStore;

  private final DatastoreExecution datastoreExecution;

  public DatastoreApplicationService(
    DatastoreEventStore eventStore,
    DatastoreCurrentStateRepository currentStateRepository,
    DatastoreExecution datastoreExecution
  ) {
    this.eventStore = eventStore;
    this.currentStateRepository = currentStateRepository;
    this.datastoreExecution = datastoreExecution;
  }

  @Transactional
  public void create(DatastoreCreation datastoreCreation) {
    currentStateRepository
      .get(datastoreCreation.datastoreId())
      .ifPresent(event -> {
        throw new DatastoreAlreadyExistException(datastoreCreation.repositoryURL());
      });

    datastoreExecution.executeDatastoreCreation(datastoreCreation);
  }

  @Transactional(readOnly = true)
  public Optional<DatastoreCurrentState> getCurrentState(DatastoreId datastoreId) {
    currentStateRepository.get(datastoreId).orElseThrow(() -> new DatastoreNotFoundException(datastoreId));

    return currentStateRepository.get(datastoreId);
  }

  @Transactional(readOnly = true)
  public List<DatastoreCurrentState> findAllCurrentStates() {
    return currentStateRepository.findAll();
  }

  @Transactional(readOnly = true)
  public DatastoreHistory getHistory(DatastoreId datastoreId) {
    return eventStore.getHistory(datastoreId);
  }
}
