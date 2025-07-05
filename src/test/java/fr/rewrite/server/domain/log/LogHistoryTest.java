package fr.rewrite.server.domain.log;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.DatastoreId;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class LogHistoryTest {

  @Test
  void shouldCreateLogHistory() {
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    Instant startTime = Instant.now();
    Instant endTime = startTime.plusSeconds(60);
    List<LogEntry> logs = Collections.emptyList();

    LogHistory logHistory = new LogHistory(datastoreId, startTime, endTime, logs);

    assertThat(logHistory.datastoreId()).isEqualTo(datastoreId);
    assertThat(logHistory.startTime()).isEqualTo(startTime);
    assertThat(logHistory.endTime()).isEqualTo(endTime);
    assertThat(logHistory.logs()).isEqualTo(logs);
  }
}
