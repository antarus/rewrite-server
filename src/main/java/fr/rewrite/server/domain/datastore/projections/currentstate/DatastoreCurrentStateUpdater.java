package fr.rewrite.server.domain.datastore.projections.currentstate;

import fr.rewrite.server.domain.EventHandler;
import fr.rewrite.server.domain.datastore.event.DatastoreCreated;
import fr.rewrite.server.domain.datastore.event.DatastoreEvent;
import org.jmolecules.event.annotation.DomainEventHandler;

public class DatastoreCurrentStateUpdater implements EventHandler<DatastoreEvent> {

  private final DatastoreCurrentStateRepository currentStateRepository;

  public DatastoreCurrentStateUpdater(DatastoreCurrentStateRepository currentStateRepository) {
    this.currentStateRepository = currentStateRepository;
  }

  @Override
  @DomainEventHandler
  public void handle(DatastoreEvent event) {
    DatastoreCurrentState newState =
      switch (event) {
        case DatastoreCreated firstEvent -> DatastoreCurrentState.from(firstEvent);
        case DatastoreEvent followingEvent -> updateState(followingEvent);
      };

    currentStateRepository.save(newState);
  }

  private DatastoreCurrentState updateState(DatastoreEvent datastoreEvent) {
    DatastoreCurrentState currentState = currentStateRepository.get(datastoreEvent.datastoreId()).orElseThrow();
    return currentState.apply(datastoreEvent);
  }
}
