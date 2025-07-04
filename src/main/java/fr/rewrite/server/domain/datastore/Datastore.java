package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.StatusEnum;
import fr.rewrite.server.domain.datastore.command.DatastoreCreation;
import fr.rewrite.server.domain.datastore.event.DatastoreCreated;
import fr.rewrite.server.domain.datastore.event.DatastoreHistory;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentState;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.collection.domain.SequentialCombiner;
import fr.rewrite.server.shared.error.domain.Assert;
import org.jmolecules.ddd.annotation.AggregateRoot;

@AggregateRoot
public class Datastore {

  private final DatastoreCurrentState currentState;

  public Datastore(DatastoreHistory history) {
    Assert.notNull("history", history);
    DatastoreCreated firstEvent = history.start();

    currentState = history
      .followingEvents()
      .stream()
      .reduce(
        new DatastoreCurrentState(
          firstEvent.datastoreId(),
          firstEvent.repositoryURL(),
          StatusEnum.DATASTORE_CREATED,
          firstEvent.sequenceId(),
          false
        ),
        DatastoreCurrentState::apply,
        new SequentialCombiner<>()
      );
  }

  public Datastore(DatastoreCurrentState from) {
    Assert.notNull("from", from);
    currentState = from;
  }

  public static DatastoreCreated create(DatastoreCreation datastoreCreation) {
    Assert.notNull("datastoreCreation", datastoreCreation);
    return DatastoreCreated.from(datastoreCreation.repositoryURL());
  }

  public DatastoreId datastoreId() {
    return currentState.datastoreId();
  }

  public StatusEnum state() {
    return currentState.lastStatus();
  }

  public RepositoryURL repositoryURL() {
    return currentState.repositoryURL();
  }
}
