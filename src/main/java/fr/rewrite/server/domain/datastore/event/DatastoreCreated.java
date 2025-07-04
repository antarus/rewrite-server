package fr.rewrite.server.domain.datastore.event;

import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.repository.RepositoryURL;
import java.time.Instant;
import java.util.UUID;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record DatastoreCreated(
  DatastoreId datastoreId,
  RepositoryURL repositoryURL,
  SequenceId sequenceId,
  UUID eventId,
  Instant occurredOn
)
  implements DatastoreEvent {
  public static DatastoreCreated from(RepositoryURL repositoryURL) {
    return new DatastoreCreated(DatastoreId.from(repositoryURL), repositoryURL, SequenceId.INITIAL, UUID.randomUUID(), Instant.now());
  }
}
