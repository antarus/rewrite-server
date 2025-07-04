package fr.rewrite.server.domain.repository;

import fr.rewrite.server.domain.JobScheduler;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.log.LogPublisher;
import fr.rewrite.server.domain.repository.command.RepositoryBranchCreate;
import fr.rewrite.server.domain.repository.command.RepositoryClone;
import fr.rewrite.server.domain.repository.command.RepositoryDelete;
import fr.rewrite.server.shared.error.domain.Assert;
import java.util.concurrent.CompletableFuture;

@DomainService
public class RepositoryExecution {

  public static final String SEQUENCE_ID = "sequenceId";
  public static final String STARTING_JOB_IN_A_VIRTUAL_THREAD = "Starting job in a Virtual Thread: ";
  public static final String FINISHED_JOB_IN_A_VIRTUAL_THREAD = "Finished job in a Virtual Thread: ";
  private final JobScheduler jobScheduler;
  private final RepositoryDomainService repositoryDomainService;
  private final LogPublisher logPublisher;

  public RepositoryExecution(JobScheduler jobScheduler, RepositoryDomainService repositoryDomainService, LogPublisher logPublisher) {
    this.jobScheduler = jobScheduler;
    this.repositoryDomainService = repositoryDomainService;
    this.logPublisher = logPublisher;
  }

  public CompletableFuture<Void> executeClone(RepositoryClone cmd) {
    Assert.notNull("cmd", cmd);
    DatastoreId dsId = cmd.datastoreId();

    return jobScheduler.submitJob(dsId, () -> {
      logPublisher.info("Starting clone repository job.", dsId);
      logPublisher.debug(String.format("Details: URL=%s, ID=%s", cmd.repositoryURL(), dsId.uuid()), dsId);
      try {
        logPublisher.debug(STARTING_JOB_IN_A_VIRTUAL_THREAD + Thread.currentThread(), dsId);
        repositoryDomainService.cloneRepository(cmd);
        logPublisher.debug(FINISHED_JOB_IN_A_VIRTUAL_THREAD + Thread.currentThread(), dsId);
      } catch (Exception e) {
        logPublisher.error("Business error during clone repository: " + e.getMessage(), dsId);
        throw e;
      }
      return null;
    });
  }

  public CompletableFuture<Void> deleteRepository(RepositoryDelete cmd) {
    Assert.notNull("cmd", cmd);
    DatastoreId dsId = cmd.datastoreId();

    return jobScheduler.submitJob(dsId, () -> {
      logPublisher.info("Starting delete repository job.", dsId);
      logPublisher.debug(String.format("Details: ID=%s", dsId.uuid()), dsId);
      try {
        logPublisher.debug(STARTING_JOB_IN_A_VIRTUAL_THREAD + Thread.currentThread(), dsId);
        repositoryDomainService.deleteRepository(cmd);
        logPublisher.debug(FINISHED_JOB_IN_A_VIRTUAL_THREAD + Thread.currentThread(), dsId);
      } catch (Exception e) {
        logPublisher.error("Business error during delete repository: " + e.getMessage(), dsId);
        throw e;
      }
      return null;
    });
  }

  public CompletableFuture<Void> createBranchAndCheckout(RepositoryBranchCreate cmd) {
    Assert.notNull("cmd", cmd);
    DatastoreId dsId = cmd.datastoreId();

    return jobScheduler.submitJob(dsId, () -> {
      logPublisher.info("Starting create branch repository job.", dsId);
      logPublisher.debug(String.format("Details: ID=%sn Branch: %s", dsId.uuid(), cmd.branchName().name()), dsId);
      try {
        logPublisher.debug(STARTING_JOB_IN_A_VIRTUAL_THREAD + Thread.currentThread(), dsId);
        repositoryDomainService.createBranchAndCheckout(cmd);
        logPublisher.debug(FINISHED_JOB_IN_A_VIRTUAL_THREAD + Thread.currentThread(), dsId);
      } catch (Exception e) {
        logPublisher.error("Business error during create branch: " + e.getMessage(), dsId);
        throw e;
      }
      return null;
    });
  }
}
