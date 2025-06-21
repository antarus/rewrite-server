package fr.rewrite.server.domain;

import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.events.RepositoryCreatedEvent;
import fr.rewrite.server.domain.feature.RewriteARepo;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.domain.spi.DataRepository;
import fr.rewrite.server.domain.spi.EventBusPort;
import fr.rewrite.server.domain.spi.JobPort;
import fr.rewrite.server.domain.spi.JobSchedulerPort;
import fr.rewrite.server.shared.error.domain.Assert;
import java.util.Optional;

@DomainService
public class RepoRewriter implements RewriteARepo {

  private final RewriteConfig rewriteConfig;
  //  private final JobSchedulerPort jobSchedulerPort;
  private final JobPort jobPort;
  private final EventBusPort eventBus;

  private final DataRepository dataRepository;

  public RepoRewriter(
    RewriteConfig rewriteConfig,
    /* JobSchedulerPort jobSchedulerPort,*/JobPort jobPort,
    EventBusPort eventBus,
    DataRepository dataRepository
  ) {
    Assert.notNull("rewriteConfig", rewriteConfig);
    Assert.notNull("rewriteConfig", rewriteConfig);
    //      Assert.notNull("jobSchedulerPort",jobSchedulerPort);
    Assert.notNull("eventBus", eventBus);
    Assert.notNull("dataRepository", dataRepository);
    Assert.notNull("jobPort", jobPort);

    this.rewriteConfig = rewriteConfig;
    //      this.jobSchedulerPort = jobSchedulerPort;
    this.eventBus = eventBus;
    this.dataRepository = dataRepository;
    this.jobPort = jobPort;
  }

  @Override
  public RewriteId initARewrite(String repoUrl) {
    RepositoryURL repositoryURL = new RepositoryURL(repoUrl);
    RewriteId rewriteId = RewriteId.fromString(repositoryURL.get());
    Optional<State> optState = dataRepository.get(rewriteId);
    if (optState.isEmpty()) {
      dataRepository.save(rewriteId, State.init());
      //        eventBus.publish( RepositoryCreatedEvent.from(repoUrl));

      //        jobSchedulerPort.createDatastoreJob(rewriteId);
    }
    jobPort.createDatastoreJob(rewriteId);

    return rewriteId;
  }
}
