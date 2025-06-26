package fr.rewrite.server.domain.repository;

import static fr.rewrite.server.domain.repository.RepositoryClonedEvent.RepositoryClonedEventBuilder.aRepositoryClonedEvent;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.shared.error.domain.Assert;
import java.time.Instant;
import java.util.UUID;

public record RepositoryClonedEvent(UUID eventId, Instant occurredOn, RewriteId rewriteId) implements DomainEvent {
  protected static final class RepositoryClonedEventBuilder {

    private UUID eventId;
    private Instant occurredOn;
    private RewriteId rewriteId;

    private RepositoryClonedEventBuilder() {}

    static RepositoryClonedEventBuilder aRepositoryClonedEvent() {
      return new RepositoryClonedEventBuilder();
    }

    RepositoryClonedEvent build() {
      if (this.eventId == null) {
        eventId = UUID.randomUUID();
      }
      if (this.occurredOn == null) {
        occurredOn = Instant.now();
      }
      Assert.notNull("rewriteId", rewriteId);
      return new RepositoryClonedEvent(eventId, occurredOn, rewriteId);
    }

    RepositoryClonedEventBuilder rewriteId(UUID rewriteId) {
      this.rewriteId = RewriteId.from(rewriteId);
      return this;
    }

    RepositoryClonedEventBuilder rewriteId(RewriteId rewriteId) {
      this.rewriteId = rewriteId;
      return this;
    }
  }

  public static RepositoryClonedEvent from(RewriteId rewriteId) {
    return aRepositoryClonedEvent().rewriteId(rewriteId).build();
  }
}
