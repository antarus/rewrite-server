package fr.rewrite.server.domain.build;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.shared.error.domain.Assert;

import java.time.Instant;
import java.util.UUID;

import static fr.rewrite.server.domain.build.BuildFinishedEvent.BuildFinishedEventBuilder.aBuildFinishedEvent;

public record BuildFinishedEvent(UUID eventId, RewriteId rewriteId, Instant occurredOn) implements DomainEvent {

  public static final String REWRITE_ID = "rewriteId";

  public BuildFinishedEvent {
    Assert.notNull("eventId", eventId);
    Assert.field("occurredOn", occurredOn).inPast();
    Assert.notNull(REWRITE_ID, rewriteId);
  }

  protected static final class BuildFinishedEventBuilder {

    private UUID eventId;
    private RewriteId rewriteId;
    private Instant occurredOn;

    private BuildFinishedEventBuilder() {}

    static BuildFinishedEventBuilder aBuildFinishedEvent() {
      return new BuildFinishedEventBuilder();
    }

    BuildFinishedEvent build() {
      if (this.eventId == null) {
        eventId = UUID.randomUUID();
      }
      if (this.occurredOn == null) {
        occurredOn = Instant.now();
      }
      Assert.notNull(REWRITE_ID, rewriteId);
      return new BuildFinishedEvent(eventId, rewriteId, occurredOn);
    }

    BuildFinishedEventBuilder rewriteId(UUID rewriteId) {
      this.rewriteId = RewriteId.from(rewriteId);
      return this;
    }

    BuildFinishedEventBuilder rewriteId(RewriteId rewriteId) {
      this.rewriteId = rewriteId;
      return this;
    }
  }

  public static BuildFinishedEvent from(RewriteId rewriteId) {
    Assert.notNull(REWRITE_ID,rewriteId);
    return aBuildFinishedEvent().rewriteId(rewriteId).build();
  }
}
