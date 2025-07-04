package fr.rewrite.server.infrastructure.secondary;

import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.datastore.DatastoreEventStore;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.datastore.event.DatastoreCreated;
import fr.rewrite.server.domain.datastore.event.DatastoreEvent;
import fr.rewrite.server.domain.datastore.event.DatastoreHistory;
import fr.rewrite.server.infrastructure.secondary.repository.JGitAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jmolecules.architecture.hexagonal.Adapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class InMemoryDatastoreEventStore implements DatastoreEventStore, InMemorySecondaryAdapter {

  private final Map<DatastoreId, List<DatastoreEvent>> histories = new HashMap<>();
  private final Map<DatastoreId, List<SequenceId>> existingSequenceIds = new HashMap<>();
  private static final Logger log = LoggerFactory.getLogger(InMemoryDatastoreEventStore.class);

  public void loadStub() {}

  @Override
  public SequenceId nextSequenceId(DatastoreId datastoreId) {
    if (histories.containsKey(datastoreId)) {
      SequenceId seqId = existingSequenceIds.get(datastoreId).stream().max(SequenceId::compareTo).orElseThrow();
      return new SequenceId(seqId.value() + 1);
    } else {
      return SequenceId.INITIAL;
    }
  }

  @Override
  public void save(DatastoreEvent event) {
    histories.computeIfAbsent(event.datastoreId(), key -> new ArrayList<>()).add(event);

    List<SequenceId> sequenceIds = existingSequenceIds.computeIfAbsent(event.datastoreId(), key -> new ArrayList<>());
    if (sequenceIds.contains(event.sequenceId())) {
      log.info("Sequence Id");
      log.info(sequenceIds.stream().map(sequenceId -> String.valueOf(sequenceId.value())).collect(Collectors.joining()));
      throw new IllegalArgumentException("SequenceId already exists");
    }
    sequenceIds.add(event.sequenceId());
  }

  @Override
  public void save(List<DatastoreEvent> events) {
    events.forEach(this::save);
  }

  @Override
  public DatastoreHistory getHistory(DatastoreId datastoreId) {
    var events = histories.get(datastoreId);
    var followingEvents = events.stream().skip(1).toList();
    return new DatastoreHistory((DatastoreCreated) events.getFirst(), followingEvents);
  }

  @Override
  public Stream<DatastoreEvent> stream() {
    return histories.values().stream().flatMap(List::stream);
  }

  @Override
  public void reset() {
    histories.clear();
    existingSequenceIds.clear();
  }
}
