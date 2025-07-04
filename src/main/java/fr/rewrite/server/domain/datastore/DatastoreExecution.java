package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.JobScheduler;
import fr.rewrite.server.domain.datastore.command.DatastoreCreation;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.log.LogPublisher;
import fr.rewrite.server.shared.error.domain.Assert;
import java.util.concurrent.CompletableFuture;

@DomainService
public class DatastoreExecution {

  private final JobScheduler jobScheduler;
  private final DatastoreDomainService datastoreDomainService;
  private final LogPublisher logPublisher;

  public DatastoreExecution(JobScheduler jobScheduler, DatastoreDomainService datastoreDomainService, LogPublisher logPublisher) {
    this.jobScheduler = jobScheduler;
    this.datastoreDomainService = datastoreDomainService;
    this.logPublisher = logPublisher;
  }

  public CompletableFuture<Void> executeDatastoreCreation(DatastoreCreation datastoreCreation) {
    Assert.notNull("datastoreCreation", datastoreCreation);
    DatastoreId dsId = datastoreCreation.datastoreId();

    return jobScheduler.submitJob(dsId, () -> {
      // Log initial state
      logPublisher.info("Starting datastore creation job.", dsId);
      logPublisher.debug(String.format("Details: URL=%s, ID=%s", datastoreCreation.repositoryURL(), dsId.uuid()), dsId);
      try {
        logPublisher.debug("Starting job in a Virtual Thread: " + Thread.currentThread(), dsId);

        datastoreDomainService.createDatastore(datastoreCreation);

        logPublisher.debug("Finished job in a Virtual Thread: " + Thread.currentThread(), dsId);
      } catch (Exception e) {
        logPublisher.error("Business error during datastore creation: " + e.getMessage(), dsId);
        throw e;
      }
      return null;
    });
  }
}
