package fr.rewrite.server.domain.datastore.event;

import fr.rewrite.server.domain.SequenceId;
import java.util.List;
import java.util.stream.Stream;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record DatastoreHistory(DatastoreCreated start, List<DatastoreEvent> followingEvents) {
  public DatastoreHistory(DatastoreCreated start, DatastoreEvent... followingEvents) {
    this(start, List.of(followingEvents));
  }

  public Stream<DatastoreEvent> historyStream() {
    return Stream.concat(Stream.of(start), followingEvents.stream());
  }

  public SequenceId lastSequenceId() {
    return historyStream().map(DatastoreEvent::sequenceId).max(SequenceId::compareTo).orElseThrow();
  }
}
