package fr.rewrite.server.domain.repository;

import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.events.LoggingEvent;
import fr.rewrite.server.domain.spi.EventBusPort;

@DomainService
public class RepositoryWorker {

  private final RepositoryPort repositoryPort;
  private final EventBusPort eventBus;

  public RepositoryWorker(RepositoryPort repositoryPort, EventBusPort eventBus) {
    this.repositoryPort = repositoryPort;
    this.eventBus = eventBus;
  }

  public void cloneARepository(RepositoryURL repositoryURL, Credentials credential) {
    repositoryPort.cloneRepository(repositoryURL, credential);

    eventBus.publish(LoggingEvent.from("createADatastore"));
  }
}
