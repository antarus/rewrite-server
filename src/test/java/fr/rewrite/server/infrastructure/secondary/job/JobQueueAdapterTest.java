package fr.rewrite.server.infrastructure.secondary.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.datastore.DatastoreWorker;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

@UnitTest
class JobQueueAdapterTest {

  private JobQueueAdapter jobQueueAdapter;
  private ListAppender<ILoggingEvent> listAppender;
  private Logger logger;

  private DatastoreWorker mockDatastoreCreator;

  @BeforeEach
  void setUp() {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    logger = loggerContext.getLogger(JobQueueAdapter.class.getName());
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    logger.setLevel(Level.DEBUG);

    mockDatastoreCreator = mock(DatastoreWorker.class);
    jobQueueAdapter = new JobQueueAdapter(mockDatastoreCreator);

    ReflectionTestUtils.setField(jobQueueAdapter, "poolSize", 1);

    jobQueueAdapter.init();

    await()
      .atMost(500, TimeUnit.MILLISECONDS)
      .until(() -> {
        List<ILoggingEvent> logs = listAppender.list;
        return logs
          .stream()
          .anyMatch(e -> e.getFormattedMessage().contains("JobQueueAdapter initialized with a thread pool of 1 threads."));
      });
    listAppender.list.clear();
  }

  @AfterEach
  void tearDown() throws InterruptedException {
    if (jobQueueAdapter != null) {
      jobQueueAdapter.shutdown();
      ExecutorService executorService = (ExecutorService) ReflectionTestUtils.getField(jobQueueAdapter, "executorService");
      if (executorService != null && !executorService.isTerminated()) {
        executorService.awaitTermination(5, TimeUnit.SECONDS);
      }
    }
    logger.detachAppender(listAppender);
    listAppender.stop();
  }

  @Test
  void createDatastoreJob_shouldEnqueueJobAndTriggerDatastoreCreation() {
    RewriteId rewriteId = new RewriteId(UUID.randomUUID());

    jobQueueAdapter.createDatastoreJob(rewriteId);

    await()
      .atMost(500, TimeUnit.MILLISECONDS)
      .untilAsserted(() -> {
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs.stream().map(ILoggingEvent::getFormattedMessage)).containsSubsequence(
          "Job enqueued successfully. Current queue size: 1",
          "Job taken from queue and submitted for execution."
        );
      });

    List<ILoggingEvent> logs = listAppender.list;
    assertThat(logs.stream().map(ILoggingEvent::getFormattedMessage)).containsSubsequence(
      "Job enqueued successfully. Current queue size: 1",
      "Job taken from queue and submitted for execution."
    );
  }

  @Test
  void init_shouldInitializeThreadPoolAndStartConsumerThread() throws InterruptedException {
    jobQueueAdapter.shutdown();
    ExecutorService executorServiceFromSetup = (ExecutorService) ReflectionTestUtils.getField(jobQueueAdapter, "executorService");
    if (executorServiceFromSetup != null && !executorServiceFromSetup.isTerminated()) {
      executorServiceFromSetup.awaitTermination(5, TimeUnit.SECONDS);
    }
    listAppender.list.clear();

    jobQueueAdapter = new JobQueueAdapter(mockDatastoreCreator);
    ReflectionTestUtils.setField(jobQueueAdapter, "poolSize", 1);

    listAppender.list.clear();

    jobQueueAdapter.init();

    await()
      .atMost(500, TimeUnit.MILLISECONDS)
      .untilAsserted(() -> {
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs.stream().map(ILoggingEvent::getFormattedMessage)).anyMatch(msg ->
          msg.contains("JobQueueAdapter initialized with a thread pool of 1 threads.")
        );
      });

    ExecutorService executorService = (ExecutorService) ReflectionTestUtils.getField(jobQueueAdapter, "executorService");
    assertThat(executorService).isNotNull();
    assertThat(executorService.isShutdown()).isFalse();
  }

  @Test
  void shutdown_shouldGracefullyTerminateExecutorService() throws InterruptedException {
    RewriteId rewriteId = new RewriteId(UUID.randomUUID());
    doAnswer(invocation -> {
      TimeUnit.MILLISECONDS.sleep(100);
      return null;
    })
      .when(mockDatastoreCreator)
      .createADatastore(rewriteId);

    jobQueueAdapter.createDatastoreJob(rewriteId);

    await()
      .atMost(200, TimeUnit.MILLISECONDS)
      .until(() -> {
        BlockingQueue<Runnable> queue = (BlockingQueue<Runnable>) ReflectionTestUtils.getField(jobQueueAdapter, "jobQueue");
        return queue.isEmpty();
      });

    jobQueueAdapter.shutdown();

    ExecutorService executorService = (ExecutorService) ReflectionTestUtils.getField(jobQueueAdapter, "executorService");
    assertThat(executorService.isTerminated()).isTrue();

    List<ILoggingEvent> logs = listAppender.list;
    assertThat(logs.stream().map(ILoggingEvent::getFormattedMessage)).containsSubsequence(
      "Attempting to shut down JobQueueAdapter gracefully.",
      "JobQueueAdapter shut down cleanly."
    );
    assertThat(logs.stream().map(ILoggingEvent::getFormattedMessage)).doesNotContain(
      "JobQueueAdapter forced shutdown: some tasks may not have completed."
    );
  }

  @Test
  void shutdown_shouldForceShutdown_whenTerminationTimesOut() throws InterruptedException {
    JobQueueAdapter spyJobQueueAdapter = Mockito.spy(jobQueueAdapter);

    ExecutorService mockExecutorService = mock(ExecutorService.class);
    ReflectionTestUtils.setField(spyJobQueueAdapter, "executorService", mockExecutorService);
    ReflectionTestUtils.setField(spyJobQueueAdapter, "running", false);

    when(mockExecutorService.awaitTermination(anyLong(), any(TimeUnit.class))).thenReturn(false);
    doNothing().when(mockExecutorService).shutdown();
    when(mockExecutorService.shutdownNow()).thenReturn(Collections.emptyList());

    spyJobQueueAdapter.shutdown();

    verify(mockExecutorService).awaitTermination(30, TimeUnit.SECONDS);
    verify(mockExecutorService).shutdownNow();

    List<ILoggingEvent> logs = listAppender.list;
    assertThat(logs.stream().map(ILoggingEvent::getFormattedMessage)).contains(
      "JobQueueAdapter forced shutdown: some tasks may not have completed."
    );
  }

  @Test
  void enqueueJob_shouldHandleInterruptedException_whenQueuePutFails() throws InterruptedException {
    BlockingQueue<Runnable> mockJobQueue = mock(BlockingQueue.class);
    ReflectionTestUtils.setField(jobQueueAdapter, "jobQueue", mockJobQueue);

    doThrow(new InterruptedException("Queue interrupted")).when(mockJobQueue).put(any(Runnable.class));

    jobQueueAdapter.createDatastoreJob(new RewriteId(UUID.randomUUID()));

    List<ILoggingEvent> logs = listAppender.list;
    assertThat(logs.stream().map(ILoggingEvent::getFormattedMessage)).contains("Failed to enqueue job: interrupted.");

    assertThat(Thread.currentThread().isInterrupted()).isTrue();
  }

  @Test
  void jobConsumer_shouldHandleInterruption() throws InterruptedException {
    JobQueueAdapter testJobQueueAdapter = new JobQueueAdapter(mockDatastoreCreator);
    ReflectionTestUtils.setField(testJobQueueAdapter, "poolSize", 1);

    BlockingQueue<Runnable> mockJobQueue = mock(BlockingQueue.class);
    ReflectionTestUtils.setField(testJobQueueAdapter, "jobQueue", mockJobQueue);
    when(mockJobQueue.take()).thenThrow(new InterruptedException("Consumer thread interrupted"));

    ListAppender<ILoggingEvent> testAppender = new ListAppender<>();
    testAppender.start();
    logger.addAppender(testAppender);

    testJobQueueAdapter.init();

    await()
      .atMost(1000, TimeUnit.MILLISECONDS)
      .untilAsserted(() -> {
        List<ILoggingEvent> logs = testAppender.list;
        assertThat(logs.stream().map(ILoggingEvent::getFormattedMessage)).anyMatch(msg -> msg.contains("Job queue consumer interrupted."));
      });

    List<ILoggingEvent> logs = testAppender.list;
    assertThat(logs.stream().map(ILoggingEvent::getFormattedMessage)).contains("Job queue consumer interrupted.");
    assertThat(logs.stream().map(ILoggingEvent::getFormattedMessage)).contains("Job queue consumer stopped.");

    testJobQueueAdapter.shutdown();
    logger.detachAppender(testAppender);
    testAppender.stop();
  }
  //    @Test
  //    void jobConsumer_shouldHandleExceptionDuringJobExecution() throws InterruptedException {
  //        JobQueueAdapter testJobQueueAdapterForException = new JobQueueAdapter(mockDatastoreCreator);
  //        ReflectionTestUtils.setField(testJobQueueAdapterForException, "poolSize", 1);
  //
  //        ListAppender<ILoggingEvent> testAppenderForException = new ListAppender<>();
  //        testAppenderForException.start();
  //        logger.addAppender(testAppenderForException);
  //
  //        testJobQueueAdapterForException.init();
  //
  //        RewriteId failingRewriteId = new RewriteId(UUID.randomUUID());
  //        doThrow(new RuntimeException("Simulated job failure")).when(mockDatastoreCreator).createADatastore(failingRewriteId);
  //
  //        testJobQueueAdapterForException.createDatastoreJob(failingRewriteId);
  //
  //        await().atMost(2000, TimeUnit.MILLISECONDS)
  //                .untilAsserted(() -> {
  //                    List<ILoggingEvent> logs = testAppenderForException.list;
  //                    assertThat(logs.stream().map(ILoggingEvent::getFormattedMessage))
  //                            .anyMatch(msg -> msg.contains("Error processing job from queue."));
  //                });
  //
  //        List<ILoggingEvent> logs = testAppenderForException.list;
  //        assertThat(logs.stream().anyMatch(e -> e.getLevel() == Level.ERROR && e.getFormattedMessage().contains("Error processing job from queue.")))
  //                .isTrue();
  //        assertThat(logs.stream().filter(e -> e.getLevel() == Level.ERROR).findFirst().get().getThrowableProxy().getClassName())
  //                .contains("RuntimeException");
  //        assertThat(logs.stream().filter(e -> e.getLevel() == Level.ERROR).findFirst().get().getThrowableProxy().getMessage())
  //                .contains("Simulated job failure");
  //
  //        verify(mockDatastoreCreator).createADatastore(failingRewriteId);
  //
  //        testJobQueueAdapterForException.shutdown();
  //        logger.detachAppender(testAppenderForException);
  //        testAppenderForException.stop();
  //    }
}
