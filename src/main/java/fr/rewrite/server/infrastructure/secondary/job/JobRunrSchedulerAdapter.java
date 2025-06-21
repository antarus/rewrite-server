package fr.rewrite.server.infrastructure.secondary.job;

import fr.rewrite.server.domain.DatastoreCreator;
import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.spi.JobPort;
import fr.rewrite.server.domain.spi.JobSchedulerPort;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("jobrunr")
public class JobRunrSchedulerAdapter implements JobSchedulerPort, JobPort {

  private final DatastoreCreator datastoreCreator;
  private final JobScheduler jobScheduler;

  public JobRunrSchedulerAdapter(DatastoreCreator datastoreCreator, JobScheduler jobScheduler) {
    this.datastoreCreator = datastoreCreator;
    this.jobScheduler = jobScheduler;
  }

  @Override
  public void createDatastoreJob(RewriteId rewriteId) {
    jobScheduler.enqueue(rewriteId.get(), () -> datastoreCreator.createADatastore(rewriteId));
  }
}
