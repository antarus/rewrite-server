package fr.rewrite.server.infrastructure.primary;

import fr.rewrite.server.domain.StatusEnum;
import fr.rewrite.server.domain.datastore.DatastoreEventStore;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.datastore.event.DatastoreCreated;
import fr.rewrite.server.domain.datastore.event.DatastoreStatusChanged;
import fr.rewrite.server.domain.repository.RepositoryURL;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class InMemoryEventStoreInitializer {

  private final DatastoreEventStore eventStore;

  public InMemoryEventStoreInitializer(DatastoreEventStore eventStore) {
    this.eventStore = eventStore;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void onApplicationReady() {
    RepositoryURL repositoryURL = RepositoryURL.from("https://github.com/antarus/jhipster-lite");
    DatastoreId dsJhlite = DatastoreId.from(repositoryURL);

    eventStore.save(DatastoreCreated.from(repositoryURL));
    eventStore.save(DatastoreStatusChanged.from(dsJhlite, StatusEnum.REPOSITORY_CLONED, eventStore.nextSequenceId(dsJhlite)));
  }
}
