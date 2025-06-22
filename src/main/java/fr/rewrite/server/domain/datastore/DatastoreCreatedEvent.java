package fr.rewrite.server.domain.datastore;

import static fr.rewrite.server.domain.datastore.DatastoreCreatedEvent.DatastoreCreatedEventBuilder.aRepositoryCreatedEvent;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.shared.error.domain.Assert;
import java.time.Instant;
import java.util.UUID;

public record DatastoreCreatedEvent(UUID eventId, RewriteId rewriteId, Instant occurredOn) implements DomainEvent {
  public DatastoreCreatedEvent {
    Assert.notNull("eventId", eventId);
    Assert.field("occurredOn", occurredOn).inPast();
    Assert.notNull("rewriteId", rewriteId);
  }

  protected static final class DatastoreCreatedEventBuilder {

    private UUID eventId;
    private RewriteId rewriteId;
    private Instant occurredOn;

    private DatastoreCreatedEventBuilder() {}

    static DatastoreCreatedEventBuilder aRepositoryCreatedEvent() {
      return new DatastoreCreatedEventBuilder();
    }

    DatastoreCreatedEvent build() {
      if (this.eventId == null) {
        eventId = UUID.randomUUID();
      }
      if (this.occurredOn == null) {
        occurredOn = Instant.now();
      }
      return new DatastoreCreatedEvent(eventId, rewriteId, occurredOn);
    }

    DatastoreCreatedEventBuilder rewriteId(RewriteId rewriteId) {
      this.rewriteId = rewriteId;
      return this;
    }
  }

  public static DatastoreCreatedEvent from(RewriteId rewriteId) {
    return aRepositoryCreatedEvent().rewriteId(rewriteId).build();
  }
}
