package fr.rewrite.server.domain;

import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.events.LoggingEvent;
import fr.rewrite.server.domain.spi.DatastorePort;
import fr.rewrite.server.domain.spi.EventBusPort;
import fr.rewrite.server.shared.error.domain.Assert;
import java.nio.file.Path;

@DomainService
public class DatastoreCreator {

  private final RewriteConfig rewriteConfig;
  private final DatastorePort datastorePort;
  private final EventBusPort eventBus;

  public DatastoreCreator(RewriteConfig rewriteConfig, DatastorePort datastorePort, EventBusPort eventBus) {
    Assert.notNull("rewriteConfig", rewriteConfig);
    Assert.notNull("datastorePort", datastorePort);
    Assert.notNull("eventBus", eventBus);
    this.rewriteConfig = rewriteConfig;
    this.datastorePort = datastorePort;
    this.eventBus = eventBus;
  }

  public void createADatastore(RewriteId rewriteId) {
    datastorePort.createDatastore(Path.of(rewriteConfig.workDirectory().toString(), rewriteId.get().toString()));

    eventBus.publish(LoggingEvent.from("createADatastore"));
  }
}
