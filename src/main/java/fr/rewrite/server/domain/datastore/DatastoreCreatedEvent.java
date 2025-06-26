package fr.rewrite.server.domain.datastore;

import static fr.rewrite.server.domain.datastore.DatastoreCreatedEvent.DatastoreCreatedEventBuilder.aDatastoreCreatedEvent;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.domain.repository.RepositoryClonedEvent;
import fr.rewrite.server.shared.error.domain.Assert;
import java.time.Instant;
import java.util.UUID;

public record DatastoreCreatedEvent(UUID eventId, RewriteId rewriteId, Instant occurredOn) implements DomainEvent {
  public static final String REWRITE_ID = "rewriteId";

  public DatastoreCreatedEvent {
    Assert.notNull("eventId", eventId);
    Assert.field("occurredOn", occurredOn).inPast();
    Assert.notNull(REWRITE_ID, rewriteId);
  }

  protected static final class DatastoreCreatedEventBuilder {

    private UUID eventId;
    private RewriteId rewriteId;
    private Instant occurredOn;

    private DatastoreCreatedEventBuilder() {}

    static DatastoreCreatedEventBuilder aDatastoreCreatedEvent() {
      return new DatastoreCreatedEventBuilder();
    }

    DatastoreCreatedEvent build() {
      if (this.eventId == null) {
        eventId = UUID.randomUUID();
      }
      if (this.occurredOn == null) {
        occurredOn = Instant.now();
      }
      Assert.notNull(REWRITE_ID, rewriteId);
      return new DatastoreCreatedEvent(eventId, rewriteId, occurredOn);
    }

    DatastoreCreatedEventBuilder rewriteId(UUID rewriteId) {
      this.rewriteId = RewriteId.from(rewriteId);
      return this;
    }

    DatastoreCreatedEventBuilder rewriteId(RewriteId rewriteId) {
      this.rewriteId = rewriteId;
      return this;
    }
  }

  public static DatastoreCreatedEvent from(RewriteId rewriteId) {
    Assert.notNull(REWRITE_ID, rewriteId);
    return aDatastoreCreatedEvent().rewriteId(rewriteId).build();
  }
}
