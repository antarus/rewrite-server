package fr.rewrite.server.infrastructure.secondary.log;

import fr.rewrite.server.domain.log.LogEntry;
import fr.rewrite.server.domain.log.LogLevel;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsoleLogSubscriber implements LogSubscriber {

  private static final Logger log = LoggerFactory.getLogger(ConsoleLogSubscriber.class);

  private final InMemoryLogStore inMemoryLogStore;
  private final SubscriptionToken token;

  public ConsoleLogSubscriber(InMemoryLogStore inMemoryLogStore) {
    this.inMemoryLogStore = inMemoryLogStore;
    token = inMemoryLogStore.subscribe(this, LogLevel.TRACE, null);
  }

  @PreDestroy
  void destroy() {
    inMemoryLogStore.unsubscribe(token);
  }

  @Override
  public void onNewLogEntry(LogEntry logEntry) {
    switch (logEntry.level()) {
      case TRACE -> log.trace(logEntry.message());
      case DEBUG -> log.debug(logEntry.message());
      case INFO -> log.info(logEntry.message());
      case WARN -> log.warn(logEntry.message());
      case ERROR -> log.error(logEntry.message());
    }
  }
}
