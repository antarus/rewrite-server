package fr.rewrite.server.domain.events;

import static fr.rewrite.server.domain.events.LoggingEvent.LoggingEventBuilder.aLoggingEventEvent;

import java.time.Instant;
import java.util.UUID;

public record LoggingEvent(UUID eventId, Instant occurredOn, String log) implements DomainEvent {
  protected static final class LoggingEventBuilder {

    private UUID eventId;
    private Instant occurredOn;
    private String log;

    private LoggingEventBuilder() {}

    static LoggingEventBuilder aLoggingEventEvent() {
      return new LoggingEventBuilder();
    }

    LoggingEvent build() {
      if (this.eventId == null) {
        eventId = UUID.randomUUID();
      }
      if (this.occurredOn == null) {
        occurredOn = Instant.now();
      }
      return new LoggingEvent(eventId, occurredOn, log);
    }

    LoggingEventBuilder log(String log) {
      this.log = log;
      return this;
    }
  }

  public static LoggingEvent from(String log) {
    return aLoggingEventEvent().log(log).build();
  }
}
