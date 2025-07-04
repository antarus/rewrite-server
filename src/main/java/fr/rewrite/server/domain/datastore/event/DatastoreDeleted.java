package fr.rewrite.server.domain.datastore.event;

import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.datastore.DatastoreId;
import java.time.Instant;
import java.util.UUID;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record DatastoreDeleted(DatastoreId datastoreId, SequenceId sequenceId, UUID eventId, Instant occurredOn) implements DatastoreEvent {
  public static DatastoreDeleted from(DatastoreId datastoreId, SequenceId sequenceId) {
    return new DatastoreDeleted(datastoreId, sequenceId, UUID.randomUUID(), Instant.now());
  }
}
