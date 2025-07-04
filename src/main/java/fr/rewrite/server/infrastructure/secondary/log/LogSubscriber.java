package fr.rewrite.server.infrastructure.secondary.log;

import fr.rewrite.server.domain.log.LogEntry;

public interface LogSubscriber {
  void onNewLogEntry(LogEntry logEntry);
}
