package fr.rewrite.server.domain.events;

import static fr.rewrite.server.domain.events.TestEvent.TestEventBuilder.aTestEventEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record TestEvent(UUID eventId, Instant occurredOn, String log) implements DomainEvent {
  protected static final class TestEventBuilder {

    private UUID eventId;
    private Instant occurredOn;
    private String log;

    private TestEventBuilder() {}

    static TestEventBuilder aTestEventEvent() {
      return new TestEventBuilder();
    }

    TestEvent build() {
      if (this.eventId == null) {
        eventId = UUID.randomUUID();
      }
      if (this.occurredOn == null) {
        occurredOn = Instant.now();
      }
      return new TestEvent(eventId, occurredOn, log);
    }

    TestEventBuilder log(String log) {
      this.log = log;
      return this;
    }
  }

  public static TestEvent from(String log) {
    return aTestEventEvent().log(log).build();
  }
}
