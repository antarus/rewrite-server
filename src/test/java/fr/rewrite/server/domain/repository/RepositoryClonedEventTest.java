package fr.rewrite.server.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.domain.events.LoggingEvent;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class RepositoryClonedEventTest {

  @Test
  void shoudlTrowExceptionIfRewridIsNull() throws IOException {
    assertThatThrownBy(() -> RepositoryClonedEvent.from(null)).isInstanceOf(MissingMandatoryValueException.class);
  }

  @Test
  void shoudlTrowExceptionIfRewridIsNull2() throws IOException {
    assertThatThrownBy(() ->
      RepositoryClonedEvent.RepositoryClonedEventBuilder.aRepositoryClonedEvent().rewriteId((RewriteId) null).build()
    ).isInstanceOf(MissingMandatoryValueException.class);
  }

  @Test
  void from_shouldCreateEventWithDefaultValue() {
    LoggingEvent event = LoggingEvent.from("test");
    assertThat(event).isNotNull();
    assertThat(event.occurredOn()).isNotNull();
    assertThat(event.eventId()).isNotNull();
  }

  @Test
  void from_shouldCreateEventWithProvidedUuid() {
    RewriteId rewriteId = RewriteId.from(UUID.fromString("80417389-6f94-485e-8a75-2f4e380e0404"));
    RepositoryClonedEvent event = RepositoryClonedEvent.from(rewriteId);
    assertThat(event).isNotNull();
    assertThat(event.rewriteId()).isEqualTo(rewriteId);
  }

  @Test
  void from_shouldGenerateEventIdAutomatically() {
    RewriteId rewriteId = RewriteId.from(UUID.fromString("80417389-6f94-485e-8a75-2f4e380e0405"));
    RepositoryClonedEvent event = RepositoryClonedEvent.from(rewriteId);
    assertThat(event).isNotNull();
    assertThat(event.eventId()).isNotNull();
  }

  @Test
  void from_shouldGenerateOccurredOnAutomatically() {
    RewriteId rewriteId = RewriteId.from(UUID.fromString("80417389-6f94-485e-8a75-2f4e380e0405"));
    Instant beforeCreation = Instant.now();
    RepositoryClonedEvent event = RepositoryClonedEvent.from(rewriteId);

    Instant afterCreation = Instant.now();

    assertThat(event).isNotNull();
    assertThat(event.occurredOn()).isNotNull();
    assertThat(event.occurredOn())
      .isAfterOrEqualTo(beforeCreation.minus(1, ChronoUnit.SECONDS))
      .isBeforeOrEqualTo(afterCreation.plus(1, ChronoUnit.SECONDS));
  }

  @Test
  void eventIdAndOccurredOnAreImmutableAfterCreation() {
    RewriteId rewriteId = RewriteId.from(UUID.fromString("80417389-6f94-485e-8a75-2f4e380e0405"));
    RepositoryClonedEvent event = RepositoryClonedEvent.from(rewriteId);

    UUID initialEventId = event.eventId();
    Instant initialOccurredOn = event.occurredOn();
    RepositoryClonedEvent anotherEvent = RepositoryClonedEvent.from(rewriteId);

    assertThat(event.eventId()).isEqualTo(initialEventId);
    assertThat(event.occurredOn()).isEqualTo(initialOccurredOn);
    assertThat(event.eventId()).isNotEqualTo(anotherEvent.eventId());
    assertThat(event.occurredOn()).isNotEqualTo(anotherEvent.occurredOn());
  }

  @Test
  void eventIsADomainEvent() {
    RewriteId rewriteId = RewriteId.from(UUID.fromString("80417389-6f94-485e-8a75-2f4e380e0405"));
    RepositoryClonedEvent event = RepositoryClonedEvent.from(rewriteId);
    assertThat(event).isInstanceOf(DomainEvent.class);
  }
}
