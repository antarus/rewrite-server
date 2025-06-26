package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.events.EventBusPort;

@DomainService
public class DatastoreWorker {

  private final DatastorePort datastorePort;
  private final EventBusPort eventBus;

  public DatastoreWorker(DatastorePort datastorePort, EventBusPort eventBus) {
    this.datastorePort = datastorePort;
    this.eventBus = eventBus;
  }

  public void createADatastore(RewriteId rewriteId) {
    datastorePort.createDatastore(rewriteId);
    eventBus.publish(DatastoreCreatedEvent.from(rewriteId));
  }

  public Datastore getDatastore(RewriteId rewriteId) {
    return datastorePort.getDatastore(rewriteId);
  }
}
