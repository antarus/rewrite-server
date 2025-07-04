package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.EventPublisher;
import fr.rewrite.server.domain.datastore.command.DatastoreCreation;
import fr.rewrite.server.domain.datastore.event.DatastoreCreated;
import fr.rewrite.server.domain.datastore.event.DatastoreEvent;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentStateRepository;
import fr.rewrite.server.domain.ddd.DomainService;

@DomainService
public class DatastoreDomainService {

  private final DatastoreEventStore eventStore;
  private final EventPublisher<DatastoreEvent> eventPublisher;

  private final DatastorePort datastorePort;

  public DatastoreDomainService(
    DatastoreEventStore eventStore,
    EventPublisher<DatastoreEvent> eventPublisher,
    DatastorePort datastorePort,
    DatastoreCurrentStateRepository currentStateRepository
  ) {
    this.eventStore = eventStore;
    this.eventPublisher = eventPublisher;
    this.datastorePort = datastorePort;
  }

  public void createDatastore(DatastoreCreation datastoreCreation) {
    DatastoreCreated event = Datastore.create(datastoreCreation);
    datastorePort.provisionDatastore(datastoreCreation.datastoreId());
    eventStore.save(event);
    eventPublisher.publish(event);
  }

  public boolean saveObjectToCache(DatastoreId dsId, String filename, DatastoreSavable object) {
    return this.datastorePort.saveObjectToDsCache(dsId, filename, object);
  }

  public DatastoreSavable getObjectToDsCache(DatastoreId dsId, String filename) {
    return this.datastorePort.getObjectToDsCache(dsId, filename);
  }
}
