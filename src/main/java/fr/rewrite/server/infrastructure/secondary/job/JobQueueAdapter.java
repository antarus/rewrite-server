package fr.rewrite.server.infrastructure.secondary.job;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.datastore.DatastoreWorker;
import fr.rewrite.server.domain.repository.Credentials;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.domain.spi.JobPort;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!jobrunr")
public class JobQueueAdapter implements JobPort {

  @Value("${job.queue.pool.size:5}")
  private int poolSize;

  private static final Logger log = LoggerFactory.getLogger(JobQueueAdapter.class);
  private final BlockingQueue<Runnable> jobQueue = new LinkedBlockingQueue<>();
  private ExecutorService executorService;
  private volatile boolean running = true;
  private final DatastoreWorker datastoreWorker;

  public JobQueueAdapter(DatastoreWorker datastoreWorker) {
    this.datastoreWorker = datastoreWorker;
  }

  @PostConstruct
  public void init() {
    this.executorService = Executors.newFixedThreadPool(poolSize);
    log.info("JobQueueAdapter initialized with a thread pool of {} threads.", poolSize);

    new Thread(
      () -> {
        while (running) {
          try {
            Runnable job = jobQueue.take();
            executorService.submit(job);
            log.debug("Job taken from queue and submitted for execution.");
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Job queue consumer interrupted.", e);
            running = false;
          } catch (Exception e) {
            log.error("Error processing job from queue.", e);
            //TODO
          }
        }
        log.info("Job queue consumer stopped.");
      },
      "JobQueueConsumer"
    ).start();
  }

  private void enqueueJob(Runnable job) {
    try {
      jobQueue.put(job);
      log.info("Job enqueued successfully. Current queue size: {}", jobQueue.size());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Failed to enqueue job: interrupted.", e);
    }
  }

  @PreDestroy
  public void shutdown() {
    running = false;
    executorService.shutdown();
    log.info("Attempting to shut down JobQueueAdapter gracefully.");
    try {
      if (!executorService.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)) {
        executorService.shutdownNow();
        log.warn("JobQueueAdapter forced shutdown: some tasks may not have completed.");
      } else {
        log.info("JobQueueAdapter shut down cleanly.");
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
      log.warn("JobQueueAdapter shutdown interrupted: some tasks may not have completed.");
    }
  }

  @Override
  public void createDatastoreJob(RewriteId rewriteId) {
    enqueueJob(() -> datastoreWorker.createADatastore(rewriteId));
  }

  @Override
  public void cloneRepository(RepositoryURL repositoryURL, Credentials credential) {
    throw new NotImplementedException();
  }
}
