package fr.rewrite.server.domain.repository;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.events.EventBusPort;

@DomainService
public class RepositoryWorker {

  private final RepositoryPort repositoryPort;
  private final EventBusPort eventBus;

  public RepositoryWorker(RepositoryPort repositoryPort, EventBusPort eventBus) {
    this.repositoryPort = repositoryPort;
    this.eventBus = eventBus;
  }

  public void cloneARepository(RepositoryURL repositoryURL) {
    repositoryPort.cloneRepository(repositoryURL);
    eventBus.publish(RepositoryClonedEvent.from(RewriteId.from(repositoryURL)));
  }

  public void cloneARepository(RepositoryURL repositoryURL, Credentials credential) {
    repositoryPort.cloneRepository(repositoryURL, credential);
    eventBus.publish(RepositoryClonedEvent.from(RewriteId.from(repositoryURL)));
  }

  public void createBranch(RewriteId rewriteId, String currentBranch) {
    repositoryPort.createBranch(rewriteId, currentBranch);
    eventBus.publish(BranchCreatedEvent.from(rewriteId));
  }
}
