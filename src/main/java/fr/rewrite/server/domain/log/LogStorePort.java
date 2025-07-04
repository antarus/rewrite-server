package fr.rewrite.server.domain.log;

public interface LogStorePort {
  void addLog(LogEntry entry);
}
