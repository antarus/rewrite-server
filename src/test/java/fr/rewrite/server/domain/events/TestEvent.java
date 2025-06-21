package fr.rewrite.server.domain.events;

import static fr.rewrite.server.domain.events.TestEvent.TestEventBuilder.aTestEventEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestEvent(String eventId, LocalDateTime occurredOn, String log) implements DomainEvent {
  protected static final class TestEventBuilder {

    private String eventId;
    private LocalDateTime occurredOn;
    private String log;

    private TestEventBuilder() {}

    static TestEventBuilder aTestEventEvent() {
      return new TestEventBuilder();
    }

    TestEvent build() {
      if (this.eventId == null) {
        eventId = UUID.randomUUID().toString();
      }
      if (this.occurredOn == null) {
        occurredOn = LocalDateTime.now();
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
