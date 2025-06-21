package fr.rewrite.server.domain.events;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
public class RepositoryCreatedEventTest {

  @Test
  void from_shouldCreateEventWithProvidedPath() {
    String expectedPath = "/my/new/repo";
    RepositoryCreatedEvent event = RepositoryCreatedEvent.from(expectedPath);
    assertThat(event).isNotNull();
    assertThat(event.path()).isEqualTo(expectedPath);
  }

  @Test
  void from_shouldGenerateEventIdAutomatically() {
    String path = "/another/repo";
    RepositoryCreatedEvent event = RepositoryCreatedEvent.from(path);
    assertThat(event).isNotNull();
    assertThat(event.eventId()).isNotNull();
    assertThat(event.eventId()).isNotEmpty();
    assertThat(UUID.fromString(event.eventId())).isNotNull();
  }

  @Test
  void from_shouldGenerateOccurredOnAutomatically() {
    String path = "/yet/another/repo";
    LocalDateTime beforeCreation = LocalDateTime.now();
    RepositoryCreatedEvent event = RepositoryCreatedEvent.from(path);

    LocalDateTime afterCreation = LocalDateTime.now();

    assertThat(event).isNotNull();
    assertThat(event.occurredOn()).isNotNull();
    assertThat(event.occurredOn())
      .isAfterOrEqualTo(beforeCreation.minus(1, ChronoUnit.SECONDS))
      .isBeforeOrEqualTo(afterCreation.plus(1, ChronoUnit.SECONDS));
  }

  @Test
  void eventIdAndOccurredOnAreImmutableAfterCreation() {
    String path = "/test/immutable";
    RepositoryCreatedEvent event = RepositoryCreatedEvent.from(path);

    String initialEventId = event.eventId();
    LocalDateTime initialOccurredOn = event.occurredOn();
    RepositoryCreatedEvent anotherEvent = RepositoryCreatedEvent.from(path);

    assertThat(event.eventId()).isEqualTo(initialEventId);
    assertThat(event.occurredOn()).isEqualTo(initialOccurredOn);
    assertThat(event.eventId()).isNotEqualTo(anotherEvent.eventId());
    assertThat(event.occurredOn()).isNotEqualTo(anotherEvent.occurredOn());
  }

  @Test
  void eventIsADomainEvent() {
    String path = "/test/interface";
    RepositoryCreatedEvent event = RepositoryCreatedEvent.from(path);
    assertThat(event).isInstanceOf(DomainEvent.class);
  }
}
