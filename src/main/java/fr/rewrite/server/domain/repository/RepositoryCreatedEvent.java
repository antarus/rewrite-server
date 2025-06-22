package fr.rewrite.server.domain.repository;

import static fr.rewrite.server.domain.repository.RepositoryCreatedEvent.RepositoryCreatedEventBuilder.aRepositoryCreatedEvent;

import fr.rewrite.server.domain.events.DomainEvent;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record RepositoryCreatedEvent(UUID eventId, Instant occurredOn, String path) implements DomainEvent {
  protected static final class RepositoryCreatedEventBuilder {

    private UUID eventId;
    private Instant occurredOn;
    private String path;

    private RepositoryCreatedEventBuilder() {}

    static RepositoryCreatedEventBuilder aRepositoryCreatedEvent() {
      return new RepositoryCreatedEventBuilder();
    }

    RepositoryCreatedEvent build() {
      if (this.eventId == null) {
        eventId = UUID.randomUUID();
      }
      if (this.occurredOn == null) {
        occurredOn = Instant.now();
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
