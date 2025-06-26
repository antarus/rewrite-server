package fr.rewrite.server.domain.repository;

import static fr.rewrite.server.domain.repository.BranchCreatedEvent.BranchCreatedEventBuilder.aBranchCreatedEvent;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.shared.error.domain.Assert;
import java.time.Instant;
import java.util.UUID;

public record BranchCreatedEvent(UUID eventId, Instant occurredOn, RewriteId rewriteId) implements DomainEvent {
  public static BranchCreatedEvent from(RewriteId rewriteId) {
    return aBranchCreatedEvent().rewriteId(rewriteId).build();
  }

  protected static final class BranchCreatedEventBuilder {

    private UUID eventId;
    private Instant occurredOn;
    private RewriteId rewriteId;

    private BranchCreatedEventBuilder() {}

    static BranchCreatedEventBuilder aBranchCreatedEvent() {
      return new BranchCreatedEventBuilder();
    }

    BranchCreatedEvent build() {
      if (this.eventId == null) {
        eventId = UUID.randomUUID();
      }
      if (this.occurredOn == null) {
        occurredOn = Instant.now();
      }
      Assert.notNull("rewriteId", rewriteId);
      return new BranchCreatedEvent(eventId, occurredOn, rewriteId);
    }

    BranchCreatedEventBuilder rewriteId(RewriteId rewriteId) {
      this.rewriteId = rewriteId;
      return this;
    }
  }
}
