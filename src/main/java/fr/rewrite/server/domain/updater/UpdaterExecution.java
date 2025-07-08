package fr.rewrite.server.domain.updater;

import fr.rewrite.server.domain.JobScheduler;
import fr.rewrite.server.domain.build.BuildDomainService;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.log.LogPublisher;
import fr.rewrite.server.domain.updater.command.UpdaterCommand;
import fr.rewrite.server.shared.error.domain.Assert;
import java.util.concurrent.CompletableFuture;

@DomainService
public class UpdaterExecution {

  private final JobScheduler jobScheduler;
  private final UpdaterDomainService updaterDomainService;
  private final LogPublisher logPublisher;

  public UpdaterExecution(JobScheduler jobScheduler, UpdaterDomainService updaterDomainService, LogPublisher logPublisher) {
    this.jobScheduler = jobScheduler;
    this.updaterDomainService = updaterDomainService;
    this.logPublisher = logPublisher;
  }

  public CompletableFuture<Void> updateProject(UpdaterCommand cmd) {
    Assert.notNull("cmd", cmd);
    DatastoreId dsId = cmd.datastoreId();

    return jobScheduler.submitJob(dsId, () -> {
      logPublisher.info("Starting update project job.", dsId);
      logPublisher.debug(String.format("Details:  ID=%s, Recipe=%s", dsId.uuid(), cmd.recipe().name()), dsId);
      try {
        logPublisher.debug("Starting job in a Virtual Thread: " + Thread.currentThread(), dsId);

        updaterDomainService.updateProject(cmd);

        logPublisher.debug("Finished job in a Virtual Thread: " + Thread.currentThread(), dsId);
      } catch (Exception e) {
        logPublisher.error("Business error during update project: " + e.getMessage(), dsId);
        throw e;
      }
      return null;
    });
  }
}
