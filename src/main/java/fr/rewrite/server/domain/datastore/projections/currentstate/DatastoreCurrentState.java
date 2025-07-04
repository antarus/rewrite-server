package fr.rewrite.server.domain.datastore.projections.currentstate;

import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.StatusEnum;
import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.datastore.event.*;
import fr.rewrite.server.domain.repository.RepositoryURL;
import org.jmolecules.architecture.cqrs.QueryModel;

@QueryModel
public record DatastoreCurrentState(
  DatastoreId datastoreId,
  RepositoryURL repositoryURL,
  StatusEnum lastStatus,
  SequenceId currentSequenceId,
  boolean deleted
) {
  public static DatastoreCurrentState from(DatastoreCreated datastoreCreated) {
    return new DatastoreCurrentState(
      datastoreCreated.datastoreId(),
      datastoreCreated.repositoryURL(),
      StatusEnum.DATASTORE_CREATED,
      SequenceId.INITIAL,
      false
    );
  }
  private DatastoreCurrentState withDeleted() {
    return new DatastoreCurrentState(datastoreId, repositoryURL, lastStatus, currentSequenceId, true);
  }
  private DatastoreCurrentState withSatus(StatusEnum state) {
    return new DatastoreCurrentState(datastoreId, repositoryURL, state, currentSequenceId, deleted);
  }
  public DatastoreCurrentState withSequenceId(SequenceId sequenceId) {
    return new DatastoreCurrentState(datastoreId, repositoryURL, lastStatus, sequenceId, deleted);
  }
  public DatastoreCurrentState apply(DatastoreEvent datastoreEvent) {
    return switch (datastoreEvent) {
      case DatastoreDeleted event -> this.withDeleted().withSequenceId(datastoreEvent.sequenceId());
      case DatastoreCreateErrorEvent event -> this.withSatus(StatusEnum.DATASTORE_CREATED).withSequenceId(event.sequenceId());
      case DatastoreCreated event -> throw new IllegalStateException("DatastoreCreated event is not expected as an update event");
      case DatastoreStatusChanged event -> this.withSatus(event.newStatus()).withSequenceId(event.sequenceId());
    };
  }

  public Datastore toDomain() {
    return new Datastore(this);
  }
}
