package fr.rewrite.server.domain.datastore.event;

import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.repository.RepositoryURL;
import java.time.Instant;
import java.util.UUID;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record DatastoreCreateErrorEvent(
  DatastoreId datastoreId,
  RepositoryURL repositoryURL,
  SequenceId sequenceId,
  Throwable throwable,
  UUID eventId,
  Instant occurredOn
)
  implements DatastoreEvent {
  public static DatastoreCreateErrorEvent from(
    DatastoreId datastoreId,
    RepositoryURL repositoryURL,
    SequenceId sequenceId,
    Throwable throwable
  ) {
    return new DatastoreCreateErrorEvent(datastoreId, repositoryURL, sequenceId, throwable, UUID.randomUUID(), Instant.now());
  }
}
