package fr.rewrite.server.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.DatastoreId;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class JobSchedulerTest {

  @Mock
  private Supplier<Void> jobLogic;

  @Test
  void shouldSubmitJob() {
    // Given
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    JobScheduler jobScheduler = new JobScheduler() {
      @Override
      public <T> CompletableFuture<Void> submitJob(DatastoreId dsId, Supplier<Void> jobLogic) {
        jobLogic.get();
        return CompletableFuture.completedFuture(null);
      }
    };

    // When
    CompletableFuture<Void> future = jobScheduler.submitJob(datastoreId, jobLogic);

    // Then
    assertThat(future).isCompleted();
    verify(jobLogic, times(1)).get();
  }
}
