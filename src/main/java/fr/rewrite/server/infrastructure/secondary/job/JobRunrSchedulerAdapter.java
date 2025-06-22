package fr.rewrite.server.infrastructure.secondary.job;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.datastore.DatastoreWorker;
import fr.rewrite.server.domain.repository.Credentials;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.domain.spi.JobPort;
import fr.rewrite.server.domain.spi.JobSchedulerPort;
import org.apache.commons.lang3.NotImplementedException;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("jobrunr")
public class JobRunrSchedulerAdapter implements JobSchedulerPort, JobPort {

  private final DatastoreWorker datastoreWorker;
  private final JobScheduler jobScheduler;

  public JobRunrSchedulerAdapter(DatastoreWorker datastoreWorker, JobScheduler jobScheduler) {
    this.datastoreWorker = datastoreWorker;
    this.jobScheduler = jobScheduler;
  }

  @Override
  public void createDatastoreJob(RewriteId rewriteId) {
    jobScheduler.enqueue(rewriteId.get(), () -> datastoreWorker.createADatastore(rewriteId));
  }

  @Override
  public void cloneRepository(RepositoryURL repositoryURL, Credentials credential) {
    throw new NotImplementedException();
  }
}
