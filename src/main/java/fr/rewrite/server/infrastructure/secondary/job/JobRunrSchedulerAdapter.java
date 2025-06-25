package fr.rewrite.server.infrastructure.secondary.job;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.datastore.DatastoreWorker;
import fr.rewrite.server.domain.repository.Credentials;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.domain.repository.RepositoryWorker;
import fr.rewrite.server.domain.spi.JobPort;
import fr.rewrite.server.domain.spi.JobSchedulerPort;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("jobrunr")
public class JobRunrSchedulerAdapter implements JobSchedulerPort, JobPort {

  private final DatastoreWorker datastoreWorker;
  private final JobScheduler jobScheduler;
  private final RepositoryWorker repositoryWorker;

  public JobRunrSchedulerAdapter(DatastoreWorker datastoreWorker, JobScheduler jobScheduler, RepositoryWorker repositoryWorker) {
    this.datastoreWorker = datastoreWorker;
    this.jobScheduler = jobScheduler;
    this.repositoryWorker = repositoryWorker;
  }

  @Override
  public void createDatastoreJob(RewriteId rewriteId) {
    jobScheduler.enqueue(rewriteId.get(), () -> datastoreWorker.createADatastore(rewriteId));
  }

  @Override
  public void cloneRepository(RepositoryURL repositoryURL, Credentials credential) {
    jobScheduler.enqueue(RewriteId.from(repositoryURL).get(), () -> repositoryWorker.cloneARepository(repositoryURL, credential));
  }

  @Override
  public void cloneRepository(RepositoryURL repositoryURL) {
    jobScheduler.enqueue(RewriteId.from(repositoryURL).get(), () -> repositoryWorker.cloneARepository(repositoryURL));
  }
}
