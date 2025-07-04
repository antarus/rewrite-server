package fr.rewrite.server.infrastructure.secondary.job;

import fr.rewrite.server.domain.JobScheduler;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.log.LogPublisher;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JobSchedulerService implements JobScheduler {

  private static final Logger log = LoggerFactory.getLogger(JobSchedulerService.class);
  private ExecutorService virtualThreadExecutor;

  private final ConcurrentLinkedQueue<Supplier<CompletableFuture<?>>> jobQueue = new ConcurrentLinkedQueue<>();

  private volatile boolean running = true;

  private final LogPublisher logPublisher;

  public JobSchedulerService(LogPublisher logPublisher) {
    this.logPublisher = logPublisher;
  }

  @PostConstruct
  public void init() {
    this.virtualThreadExecutor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("job-worker-", 0).factory());

    virtualThreadExecutor.submit(() -> {
      while (running) {
        try {
          Supplier<CompletableFuture<?>> jobSupplier = jobQueue.poll();
          if (jobSupplier != null) {
            jobSupplier
              .get()
              .exceptionally(ex -> {
                log.error("Error during job execution: {}", ex.getMessage());

                return null;
              });
          } else {
            TimeUnit.MILLISECONDS.sleep(10);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          log.error("Job consumer was interrupted.");
          running = false;
        } catch (Exception e) {
          log.error("Unexpected error in job consumer: {}", e.getMessage());
        }
      }
      log.info("Job consumer stopped.");
    });

    log.info("JobSchedulerService initialized. Job consumer started.");
  }

  @Override
  public <T> CompletableFuture<Void> submitJob(DatastoreId dsId, Supplier<Void> jobLogic) {
    logPublisher.debug("Add job to queue", dsId);

    CompletableFuture<Void> future = CompletableFuture.supplyAsync(jobLogic, virtualThreadExecutor);

    jobQueue.add(() -> future);
    logPublisher.trace("Job added to queue. Queue size: " + jobQueue.size(), dsId);
    return future;
  }

  @PreDestroy
  public void destroy() {
    running = false; // Stop the consumer
    if (virtualThreadExecutor != null) {
      virtualThreadExecutor.shutdown();
      try {
        if (!virtualThreadExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
          virtualThreadExecutor.shutdownNow();
          log.error("ExecutorService could not shut down gracefully.");
        }
      } catch (InterruptedException ie) {
        virtualThreadExecutor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
    log.info("JobSchedulerService stopped.");
  }
}
