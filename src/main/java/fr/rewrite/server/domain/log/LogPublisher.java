package fr.rewrite.server.domain.log;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.ddd.DomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DomainService
public class LogPublisher {

  private static final Logger log = LoggerFactory.getLogger(LogPublisher.class);

  private final LogStorePort logStore;
  private final CleanSensitiveLog cleanSensitiveLog;

  public LogPublisher(LogStorePort logStore, CleanSensitiveLog cleanSensitiveLog) {
    this.logStore = logStore;
    this.cleanSensitiveLog = cleanSensitiveLog;
  }

  public void publishLog(LogLevel level, String message, DatastoreId datastoreId) {
    LogEntry logEntry = LogEntry.from(datastoreId, level, cleanSensitiveLog.clean(message));

    logStore.addLog(logEntry);
    // TODO push SSE ?
    ////        logPusher.pushLogToStream(logEntry);

  }

  public void info(String message, DatastoreId datastoreId) {
    publishLog(LogLevel.INFO, message, datastoreId);
  }

  public void debug(String message, DatastoreId datastoreId) {
    publishLog(LogLevel.DEBUG, message, datastoreId);
  }

  public void trace(String message, DatastoreId datastoreId) {
    publishLog(LogLevel.TRACE, message, datastoreId);
  }

  public void warn(String message, DatastoreId datastoreId) {
    publishLog(LogLevel.WARN, message, datastoreId);
  }

  public void error(String message, DatastoreId datastoreId) {
    publishLog(LogLevel.ERROR, message, datastoreId);
  }
}
