package fr.rewrite.server.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.exception.DataAccessException;
import fr.rewrite.server.domain.exception.RewriteException;
import fr.rewrite.server.domain.spi.DataRepository;
import fr.rewrite.server.domain.spi.EventBusPort;
import fr.rewrite.server.domain.spi.JobPort;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class RepoRewriterTest {

  @Mock
  private RewriteConfig mockRewriteConfig;

  @Mock
  private JobPort mockJobPort;

  @Mock
  private EventBusPort mockEventBusPort;

  @Mock
  private DataRepository mockDataRepository;

  private RepoRewriter repoRewriter;

  @BeforeEach
  void setUp() {
    repoRewriter = new RepoRewriter(mockRewriteConfig, mockJobPort, mockEventBusPort, mockDataRepository);
  }

  @Test
  @DisplayName("Constructor should throw MissingMandatoryValueException when rewriteConfig is null")
  void constructor_shouldThrowExceptionWhenRewriteConfigIsNull() {
    assertThrows(MissingMandatoryValueException.class, () -> new RepoRewriter(null, mockJobPort, mockEventBusPort, mockDataRepository));
  }

  @Test
  @DisplayName("Constructor should throw MissingMandatoryValueException when jobPort is null")
  void constructor_shouldThrowExceptionWhenJobPortIsNull() {
    assertThrows(MissingMandatoryValueException.class, () ->
      new RepoRewriter(mockRewriteConfig, null, mockEventBusPort, mockDataRepository)
    );
  }

  @Test
  @DisplayName("Constructor should throw MissingMandatoryValueException when eventBus is null")
  void constructor_shouldThrowExceptionWhenEventBusIsNull() {
    assertThrows(MissingMandatoryValueException.class, () -> new RepoRewriter(mockRewriteConfig, mockJobPort, null, mockDataRepository));
  }

  @Test
  @DisplayName("Constructor should throw MissingMandatoryValueException when dataRepository is null")
  void constructor_shouldThrowExceptionWhenDataRepositoryIsNull() {
    assertThrows(MissingMandatoryValueException.class, () -> new RepoRewriter(mockRewriteConfig, mockJobPort, mockEventBusPort, null));
  }

  @Test
  @DisplayName("initARewrite should save new state and create job when no existing state")
  void createDatastore_shouldSaveNewStateAndCreateJob_whenNoExistingState() {
    String repoUrl = "https://github.com/test/repo1.git";
    RewriteId expectedRewriteId = RewriteId.fromString(repoUrl);

    when(mockDataRepository.get(any(RewriteId.class))).thenReturn(Optional.empty());

    RewriteId actualRewriteId = repoRewriter.createDatastore(repoUrl);

    assertThat(actualRewriteId).isEqualTo(expectedRewriteId);

    ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
    verify(mockDataRepository, times(1)).save(stateCaptor.capture());
    assertThat(stateCaptor.getValue().status()).isEqualTo(StateEnum.INIT);

    verify(mockJobPort, times(1)).createDatastoreJob(expectedRewriteId);

    verifyNoMoreInteractions(mockDataRepository);
    verifyNoMoreInteractions(mockJobPort);
    verifyNoInteractions(mockEventBusPort);
  }

  @Test
  @DisplayName("initARewrite should not save state or create job when state already exists")
  void createDatastore_shouldNotSaveStateOrCreateJob_whenStateAlreadyExists() {
    String repoUrl = "https://github.com/test/existing-repo.git";
    RewriteId expectedRewriteId = RewriteId.fromString(repoUrl);

    State existingState = new State(expectedRewriteId, StateEnum.REPO_CREATED, Instant.now().minusSeconds(3600), Instant.now());
    when(mockDataRepository.get(any(RewriteId.class))).thenReturn(Optional.of(existingState));

    RewriteId actualRewriteId = repoRewriter.createDatastore(repoUrl);

    assertThat(actualRewriteId).isEqualTo(expectedRewriteId);

    verify(mockDataRepository, never()).save(any(State.class));
    verify(mockJobPort, never()).createDatastoreJob(any(RewriteId.class));
    verify(mockDataRepository, times(1)).get(expectedRewriteId);
    verifyNoMoreInteractions(mockDataRepository);
    verifyNoInteractions(mockJobPort);
    verifyNoInteractions(mockEventBusPort);
  }

  @Test
  @DisplayName("initARewrite should propagate DataAccessException from dataRepository.get")
  void createDatastore_shouldPropagateDataAccessException_fromGet() {
    String repoUrl = "https://github.com/test/error-repo.git";
    DataAccessException expectedException = new DataAccessException("Failed to get data");

    when(mockDataRepository.get(any(RewriteId.class))).thenThrow(expectedException);

    DataAccessException thrown = assertThrows(DataAccessException.class, () -> repoRewriter.createDatastore(repoUrl));
    assertThat(thrown).isEqualTo(expectedException);

    verify(mockDataRepository, times(1)).get(any(RewriteId.class));
    verifyNoMoreInteractions(mockDataRepository);
    verifyNoInteractions(mockJobPort);
    verifyNoInteractions(mockEventBusPort);
  }

  @Test
  @DisplayName("initARewrite should propagate DataAccessException from dataRepository.save")
  void createDatastore_shouldPropagateDataAccessException_fromSave() {
    String repoUrl = "https://github.com/test/save-error-repo.git";
    DataAccessException expectedException = new DataAccessException("Failed to save data");

    when(mockDataRepository.get(any(RewriteId.class))).thenReturn(Optional.empty());
    doThrow(expectedException).when(mockDataRepository).save(any(State.class));

    DataAccessException thrown = assertThrows(DataAccessException.class, () -> repoRewriter.createDatastore(repoUrl));
    assertThat(thrown).isEqualTo(expectedException);

    verify(mockDataRepository, times(1)).get(any(RewriteId.class));
    verify(mockDataRepository, times(1)).save(any(State.class));
    verifyNoMoreInteractions(mockDataRepository);
    verifyNoInteractions(mockJobPort);
    verifyNoInteractions(mockEventBusPort);
  }

  @Test
  @DisplayName("initARewrite should handle invalid repoUrl gracefully if RewriteId.fromString throws IllegalArgumentException")
  void createDatastore_shouldHandleInvalidRepoUrl() {
    String invalidRepoUrl = "invalid-uuid-format";

    assertThrows(RewriteException.class, () -> {
      repoRewriter.createDatastore(invalidRepoUrl);
    });
    verifyNoInteractions(mockDataRepository);
    verifyNoInteractions(mockJobPort);
    verifyNoInteractions(mockEventBusPort);
  }

  @Test
  @DisplayName("initARewrite should throw MissingMandatoryValueException if repoUrl is null")
  void createDatastore_shouldThrowMissingMandatoryValueException_whenRepoUrlIsNull() {
    String repoUrl = null;
    assertThrows(MissingMandatoryValueException.class, () -> repoRewriter.createDatastore(repoUrl));

    verifyNoInteractions(mockDataRepository);
    verifyNoInteractions(mockJobPort);
    verifyNoInteractions(mockEventBusPort);
  }

  @Test
  @DisplayName("initARewrite should throw MissingMandatoryValueException if repoUrl is empty")
  void createDatastore_shouldThrowMissingMandatoryValueException_whenRepoUrlIsEmpty() {
    String repoUrl = "";
    assertThrows(MissingMandatoryValueException.class, () -> repoRewriter.createDatastore(repoUrl));

    verifyNoInteractions(mockDataRepository);
    verifyNoInteractions(mockJobPort);
    verifyNoInteractions(mockEventBusPort);
  }

  @Test
  @DisplayName("initARewrite should propagate DataAccessException if jobPort.createDatastoreJob fails")
  void createDatastore_shouldPropagateDataAccessException_whenJobPortCreationFails() {
    String repoUrl = "https://github.com/test/job-error.git";
    RewriteId expectedRewriteId = RewriteId.fromString(repoUrl);
    DataAccessException expectedException = new DataAccessException("Failed to create job");

    when(mockDataRepository.get(any(RewriteId.class))).thenReturn(Optional.empty());
    doThrow(expectedException).when(mockJobPort).createDatastoreJob(any(RewriteId.class));

    DataAccessException thrown = assertThrows(DataAccessException.class, () -> repoRewriter.createDatastore(repoUrl));
    assertThat(thrown).isEqualTo(expectedException);

    verify(mockDataRepository, times(1)).get(expectedRewriteId);
    verify(mockDataRepository, times(1)).save(any(State.class));
    verify(mockJobPort, times(1)).createDatastoreJob(expectedRewriteId);

    verifyNoMoreInteractions(mockDataRepository);
    verifyNoMoreInteractions(mockJobPort);
    verifyNoInteractions(mockEventBusPort);
  }

  @Test
  @DisplayName("initARewrite should return correct RewriteId for a given URL")
  void createDatastoreId() {
    String repoUrl = "https://github.com/myorg/my-specific-repo.git";
    RewriteId expectedRewriteId = RewriteId.fromString(repoUrl);

    when(mockDataRepository.get(expectedRewriteId)).thenReturn(Optional.empty());

    RewriteId actualRewriteId = repoRewriter.createDatastore(repoUrl);
    assertThat(actualRewriteId).isEqualTo(expectedRewriteId);

    verify(mockDataRepository).get(expectedRewriteId);
    verify(mockDataRepository).save(any(State.class));
    verify(mockJobPort).createDatastoreJob(expectedRewriteId);

    verifyNoMoreInteractions(mockDataRepository, mockJobPort, mockEventBusPort);
  }
}
