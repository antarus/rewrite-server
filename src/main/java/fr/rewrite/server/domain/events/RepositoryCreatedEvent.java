package fr.rewrite.server.domain.events;

import static fr.rewrite.server.domain.events.RepositoryCreatedEvent.RepositoryCreatedEventBuilder.aRepositoryCreatedEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record RepositoryCreatedEvent(String eventId, LocalDateTime occurredOn, String path) implements DomainEvent {
  protected static final class RepositoryCreatedEventBuilder {

    private String eventId;
    private LocalDateTime occurredOn;
    private String path;

    private RepositoryCreatedEventBuilder() {}

    static RepositoryCreatedEventBuilder aRepositoryCreatedEvent() {
      return new RepositoryCreatedEventBuilder();
    }

    RepositoryCreatedEvent build() {
      if (this.eventId == null) {
        eventId = UUID.randomUUID().toString();
      }
      if (this.occurredOn == null) {
        occurredOn = LocalDateTime.now();
      }
      return new RepositoryCreatedEvent(eventId, occurredOn, path);
    }

    RepositoryCreatedEventBuilder path(String path) {
      this.path = path;
      return this;
    }
  }

  public static RepositoryCreatedEvent from(String path) {
    return aRepositoryCreatedEvent().path(path).build();
  }
}
