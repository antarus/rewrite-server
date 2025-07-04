package fr.rewrite.server.domain.log;

import fr.rewrite.server.domain.datastore.DatastoreId;
import java.time.Instant;
import java.util.List;

public record LogHistory(DatastoreId datastoreId, Instant startTime, Instant endTime, List<LogEntry> logs) {}
