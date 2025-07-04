package fr.rewrite.server.domain.datastore.event;

import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.StatusEnum;
import fr.rewrite.server.domain.datastore.DatastoreId;
import java.time.Instant;
import java.util.UUID;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public record DatastoreStatusChanged(DatastoreId datastoreId, StatusEnum newStatus, SequenceId sequenceId, UUID eventId, Instant occurredOn)
  implements DatastoreEvent {
  public static DatastoreStatusChanged from(DatastoreId datastoreId, StatusEnum newStatus, SequenceId sequenceId) {
    return new DatastoreStatusChanged(datastoreId, newStatus, sequenceId, UUID.randomUUID(), Instant.now());
  }
}
