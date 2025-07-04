package fr.rewrite.server.infrastructure.secondary.log;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.log.LogEntry;
import fr.rewrite.server.domain.log.LogHistory;
import fr.rewrite.server.domain.log.LogLevel; // Assurez-vous que cette énumération existe dans votre domaine
import fr.rewrite.server.domain.log.LogStorePort;
import fr.rewrite.server.infrastructure.secondary.InMemorySecondaryAdapter;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InMemoryLogStore implements LogStorePort, InMemorySecondaryAdapter {

  private static final Logger log = LoggerFactory.getLogger(InMemoryLogStore.class);
  private final Map<DatastoreId, CopyOnWriteArrayList<LogEntry>> datastoreLogs = new ConcurrentHashMap<>();
  private final Map<DatastoreId, Instant> datastoreStartTimes = new ConcurrentHashMap<>();

  private final Map<SubscriptionToken, SubscriberInfo> subscribers = new ConcurrentHashMap<>();

  @Override
  public void addLog(LogEntry logEntry) {
    DatastoreId datastoreId = logEntry.datastoreId();
    datastoreLogs
      .computeIfAbsent(datastoreId, k -> {
        datastoreStartTimes.put(datastoreId, logEntry.timestamp());
        return new CopyOnWriteArrayList<>();
      })
      .add(logEntry);

    if (log.isTraceEnabled()) {
      log.trace("Stored: {} - {} (Datastore: {})", logEntry.level(), logEntry.message(), logEntry.datastoreId().uuid());
    }

    notifySubscribers(logEntry);
  }

  public List<LogEntry> getLogsByDatastoreId(DatastoreId datastoreId) {
    return datastoreLogs.getOrDefault(datastoreId, new CopyOnWriteArrayList<>());
  }

  public LogHistory getAndClearDatastoreLogHistory(DatastoreId datastoreId) {
    LogHistory history = this.getLogHistoryForDatastore(datastoreId);

    if (history != null) {
      log.info("--- Persisting LogHistory for Datastore {} ---", datastoreId.uuid());
      log.info("{}", history.datastoreId().uuid());
      log.info("----------------------------------------");

      this.clearLogsForDatastore(datastoreId);
    }
    return history;
  }

  public LogHistory getLogHistoryForDatastore(DatastoreId datastoreId) {
    List<LogEntry> logs = getLogsByDatastoreId(datastoreId);
    if (logs.isEmpty()) {
      return null;
    }

    Instant startTime = datastoreStartTimes.get(datastoreId);
    Instant endTime = logs.stream().map(LogEntry::timestamp).max(Instant::compareTo).orElse(null);

    return new LogHistory(datastoreId, startTime, endTime, logs);
  }

  public void clearLogsForDatastore(DatastoreId datastoreId) {
    datastoreLogs.remove(datastoreId);
    datastoreStartTimes.remove(datastoreId);
    log.info("Cleared logs for datastore: {}", datastoreId.uuid());
  }

  @Override
  public void reset() {
    datastoreLogs.clear();
    datastoreStartTimes.clear();
  }

  public SubscriptionToken subscribe(LogSubscriber subscriber, LogLevel minLevel, DatastoreId datastoreId) {
    SubscriptionToken token = new SubscriptionToken(UUID.randomUUID().toString());
    subscribers.put(token, new SubscriberInfo(subscriber, minLevel, datastoreId));
    log.info(
      "New subscriber added with token: {} (MinLevel: {}, DatastoreId: {})",
      token.value(),
      minLevel,
      datastoreId != null ? datastoreId.uuid() : "All"
    );
    return token;
  }

  public boolean unsubscribe(SubscriptionToken token) {
    SubscriberInfo removedSubscriber = subscribers.remove(token);
    if (removedSubscriber != null) {
      log.info("Subscriber with token {} removed.", token.value());
      return true;
    }
    log.warn("Attempted to unsubscribe with non-existent token: {}", token.value());
    return false;
  }

  private void notifySubscribers(LogEntry logEntry) {
    subscribers.forEach((token, subscriberInfo) -> {
      boolean levelMatches = logEntry.level().ordinal() >= subscriberInfo.minLevel().ordinal();
      boolean datastoreMatches = subscriberInfo.datastoreId() == null || subscriberInfo.datastoreId().equals(logEntry.datastoreId());

      if (levelMatches && datastoreMatches) {
        try {
          subscriberInfo.subscriber().onNewLogEntry(logEntry);
        } catch (Exception e) {
          log.error("Error notifying subscriber {} for log entry {}: {}", token.value(), logEntry, e.getMessage());
        }
      }
    });
  }
}
