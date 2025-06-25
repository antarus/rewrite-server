package fr.rewrite.server.domain.events;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class LoggingEventTest {

  @Test
  void from_shouldCreateEventWithDefaultValue() {
    LoggingEvent event = LoggingEvent.from("test");
    assertThat(event).isNotNull();
    assertThat(event.occurredOn()).isNotNull();
    assertThat(event.eventId()).isNotNull();
  }
}
