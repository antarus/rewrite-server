package fr.rewrite.server.domain.datastore.projections.currentstate;

import static org.mockito.Mockito.*;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.StatusEnum;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.datastore.event.DatastoreCreated;
import fr.rewrite.server.domain.datastore.event.DatastoreDeleted;
import fr.rewrite.server.domain.datastore.event.DatastoreStatusChanged;
import fr.rewrite.server.domain.repository.RepositoryURL;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DatastoreCurrentStateUpdaterTest {

  @Mock
  private DatastoreCurrentStateRepository currentStateRepository;

  private DatastoreCurrentStateUpdater updater;

  @BeforeEach
  void setUp() {
    updater = new DatastoreCurrentStateUpdater(currentStateRepository);
  }

  @Test
  void shouldHandleDatastoreCreatedEvent() {
    // Given
    RepositoryURL repoUrl = new RepositoryURL("https://github.com/test/test-repo");
    DatastoreId datastoreId = DatastoreId.from(repoUrl);
    DatastoreCreated event = DatastoreCreated.from(repoUrl);

    // When
    updater.handle(event);

    // Then
    verify(currentStateRepository, times(1)).save(any(DatastoreCurrentState.class));
  }

  @Test
  void shouldHandleDatastoreDeletedEvent() {
    // Given
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    SequenceId sequenceId = new SequenceId(1);
    DatastoreDeleted event = DatastoreDeleted.from(datastoreId, sequenceId);
    DatastoreCurrentState currentState = new DatastoreCurrentState(
      datastoreId,
      new RepositoryURL("https://github.com/test/test-repo"),
      StatusEnum.DATASTORE_CREATED,
      new SequenceId(0),
      false
    );
    when(currentStateRepository.get(datastoreId)).thenReturn(Optional.of(currentState));

    // When
    updater.handle(event);

    // Then
    verify(currentStateRepository, times(1)).save(any(DatastoreCurrentState.class));
  }

  @Test
  void shouldHandleDatastoreStatusChangedEvent() {
    // Given
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    SequenceId sequenceId = new SequenceId(1);
    DatastoreStatusChanged event = DatastoreStatusChanged.from(datastoreId, StatusEnum.BUILDING, sequenceId);
    DatastoreCurrentState currentState = new DatastoreCurrentState(
      datastoreId,
      new RepositoryURL("https://github.com/test/test-repo"),
      StatusEnum.DATASTORE_CREATED,
      new SequenceId(0),
      false
    );
    when(currentStateRepository.get(datastoreId)).thenReturn(Optional.of(currentState));

    // When
    updater.handle(event);

    // Then
    verify(currentStateRepository, times(1)).save(any(DatastoreCurrentState.class));
  }
}
