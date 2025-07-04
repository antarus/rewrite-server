package fr.rewrite.server.infrastructure.secondary.log;

import fr.rewrite.server.domain.log.LogEntry;
import fr.rewrite.server.domain.log.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SSELogSubscriber implements LogSubscriber {

  private static final Logger log = LoggerFactory.getLogger(SSELogSubscriber.class);

  private final InMemoryLogStore inMemoryLogStore;
  private final SubscriptionToken token;

  public SSELogSubscriber(InMemoryLogStore inMemoryLogStore) {
    this.inMemoryLogStore = inMemoryLogStore;
    this.token = inMemoryLogStore.subscribe(this, LogLevel.TRACE, null);
  }

  @Override
  public void onNewLogEntry(LogEntry logEntry) {
    //TODO
  }
}
