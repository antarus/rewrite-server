package fr.rewrite.server.wire.init;

import fr.rewrite.server.domain.EventPublisher;
import fr.rewrite.server.domain.datastore.DatastoreEventStore;
import fr.rewrite.server.domain.datastore.event.DatastoreDeleted;
import fr.rewrite.server.domain.datastore.event.DatastoreEvent;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentStateRepository;
import fr.rewrite.server.domain.log.LogPublisher;
import fr.rewrite.server.infrastructure.secondary.filesystem.NioFileSystemAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class StartupListener {

  private static final Logger log = LoggerFactory.getLogger(StartupListener.class);
  private final DatastoreCurrentStateRepository currentStateRepository;
  private final NioFileSystemAdapter fileSystemAdapter;
  private final DatastoreEventStore eventStore;
  private final LogPublisher logPublisher;
  private final EventPublisher<DatastoreEvent> eventPublisher;

  public StartupListener(
    DatastoreCurrentStateRepository currentStateRepository,
    NioFileSystemAdapter fileSystemAdapter,
    DatastoreEventStore eventStore,
    LogPublisher logPublisher,
    EventPublisher<DatastoreEvent> eventPublisher
  ) {
    this.currentStateRepository = currentStateRepository;

    this.fileSystemAdapter = fileSystemAdapter;
    this.eventStore = eventStore;
    this.logPublisher = logPublisher;
    this.eventPublisher = eventPublisher;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    log.info("Application ready");

    currentStateRepository
      .findAll()
      .forEach(state -> {
        if (!fileSystemAdapter.exists(state.datastoreId())) {
          logPublisher.info(
            String.format("Delete datastore %s, datastore no more exist in filesystem", state.datastoreId().toString()),
            state.datastoreId()
          );

          DatastoreDeleted datastoreDeleted = DatastoreDeleted.from(state.datastoreId(), eventStore.nextSequenceId(state.datastoreId()));
          eventStore.save(datastoreDeleted);
          eventPublisher.publish(datastoreDeleted);
        }
      });
  }
}
