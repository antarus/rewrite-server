package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.datastore.event.DatastoreEvent;
import fr.rewrite.server.domain.datastore.event.DatastoreHistory;
import java.util.List;
import java.util.stream.Stream;

public interface DatastoreEventStore {
  SequenceId nextSequenceId(DatastoreId datastoreId);
  void save(DatastoreEvent event);

  void save(List<DatastoreEvent> events);

  DatastoreHistory getHistory(DatastoreId datastoreId);

  Stream<DatastoreEvent> stream();
}
