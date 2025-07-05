package fr.rewrite.server.domain.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class LogEntryTest {

  @Test
  void shouldCreateLogEntry() {
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    LogLevel level = LogLevel.INFO;
    String message = "Test message";
    Instant timestamp = Instant.now();

    LogEntry logEntry = new LogEntry(datastoreId, level, message, timestamp);

    assertThat(logEntry.datastoreId()).isEqualTo(datastoreId);
    assertThat(logEntry.level()).isEqualTo(level);
    assertThat(logEntry.message()).isEqualTo(message);
    assertThat(logEntry.timestamp()).isEqualTo(timestamp);
  }

  @Test
  void shouldCreateLogEntryFromStaticMethod() {
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    LogLevel level = LogLevel.DEBUG;
    String message = "Another test message";

    LogEntry logEntry = LogEntry.from(datastoreId, level, message);

    assertThat(logEntry.datastoreId()).isEqualTo(datastoreId);
    assertThat(logEntry.level()).isEqualTo(level);
    assertThat(logEntry.message()).isEqualTo(message);
    assertThat(logEntry.timestamp()).isNotNull();
  }

  @Test
  void shouldThrowExceptionWhenDatastoreIdIsNull() {
    assertThatThrownBy(() -> new LogEntry(null, LogLevel.INFO, "message", Instant.now()))
      .isInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("datastoreId");
  }

  @Test
  void shouldThrowExceptionWhenLevelIsNull() {
    assertThatThrownBy(() -> new LogEntry(new DatastoreId(UUID.randomUUID()), null, "message", Instant.now()))
      .isInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("level");
  }

  @Test
  void shouldThrowExceptionWhenMessageIsNull() {
    assertThatThrownBy(() -> new LogEntry(new DatastoreId(UUID.randomUUID()), LogLevel.INFO, null, Instant.now()))
      .isInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("message");
  }

  @Test
  void shouldThrowExceptionWhenTimestampIsNull() {
    assertThatThrownBy(() -> new LogEntry(new DatastoreId(UUID.randomUUID()), LogLevel.INFO, "message", null))
      .isInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("timestamp");
  }
}
