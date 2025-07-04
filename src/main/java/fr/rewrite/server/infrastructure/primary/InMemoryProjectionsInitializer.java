package fr.rewrite.server.infrastructure.primary;

import fr.rewrite.server.domain.EventPublisher;
import fr.rewrite.server.domain.datastore.DatastoreEventStore;
import fr.rewrite.server.domain.datastore.event.DatastoreEvent;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentStateRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class InMemoryProjectionsInitializer {

  private final DatastoreEventStore eventStore;
  private final EventPublisher<DatastoreEvent> eventPublisher;

  InMemoryProjectionsInitializer(DatastoreEventStore eventStore, EventPublisher<DatastoreEvent> eventPublisher) {
    this.eventStore = eventStore;
    this.eventPublisher = eventPublisher;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationStarted() {
    eventStore.stream().forEach(eventPublisher::publish);
  }
}
