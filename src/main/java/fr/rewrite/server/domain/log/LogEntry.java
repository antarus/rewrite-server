package fr.rewrite.server.domain.log;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.shared.error.domain.Assert;
import java.time.Instant;

public record LogEntry(DatastoreId datastoreId, LogLevel level, String message, Instant timestamp) {
  public LogEntry {
    Assert.notNull("datastoreId", datastoreId);
    Assert.notNull("level", level);
    Assert.field("message", message).notNull();
    Assert.field("timestamp", timestamp).notNull();
  }

  public static LogEntry from(DatastoreId datastoreId, LogLevel level, String message) {
    return new LogEntry(datastoreId, level, message, Instant.now());
  }
}
