package fr.rewrite.server.domain.build;

import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.events.EventBusPort;

@DomainService
public class BuildWorker {

  private final BuildPort buildPort;
  private final EventBusPort eventBus;

  public BuildWorker(BuildPort buildPort, EventBusPort eventBus) {
    this.buildPort = buildPort;
    this.eventBus = eventBus;
  }

  public void buildProject(Datastore datastore) {
    buildPort.buildProject(datastore);
    eventBus.publish(BuildFinishedEvent.from(datastore.rewriteId()));
  }

}
