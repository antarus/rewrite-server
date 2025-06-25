package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.spi.JobPort;
import fr.rewrite.server.domain.state.RewriteConfig;
import fr.rewrite.server.domain.state.StateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DatastoreManagerImplTest {

  @Mock
  private RewriteConfig mockRewriteConfig;

  @Mock
  private JobPort mockJobPort;

  @Mock
  private DatastoreWorker mockDatastoreWorker;

  @Mock
  private DatastorePort mockDatastorePort;

  @Mock
  private StateRepository mockStateRepository;

  private DatastoreManagerImpl datastoreManager;

  @BeforeEach
  void setUp() {
    datastoreManager = new DatastoreManagerImpl(mockJobPort, mockStateRepository, mockDatastoreWorker);
  }
  //
  //  @Test
  //  @DisplayName("createADatastore should create datastore and publish logging event")
  //  void createADatastore_shouldCreateDatastoreAndPublishEvent() {
  //    RewriteId rewriteId =  RewriteId.from("/tmp/temp/work");
  //    Path workDirectory = Path.of("/tmp/temp/work");
  //    datastoreManager.createADatastore(rewriteId);
  //
  //    verify(mockDatastorePort, times(1)).createDatastore(rewriteId);
  //
  //    ArgumentCaptor<LoggingEvent> eventCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
  //    verify(mockEventBusPort, times(1)).publish(eventCaptor.capture());
  //    assertThat(eventCaptor.getValue().log()).isEqualTo("createADatastore");
  //
  //    verifyNoMoreInteractions(mockDatastorePort, mockEventBusPort, mockRewriteConfig);
  //  }
  //
  //  @Test
  //  @DisplayName("createADatastore should propagate RuntimeException from datastorePort.createDatastore")
  //  void createADatastore_shouldPropagateException_fromDatastorePort() {
  //    RewriteId rewriteId =  RewriteId.from("/tmp/temp/work");
  //    RuntimeException expectedException = new RuntimeException("Datastore creation failed!");
  //    doThrow(expectedException).when(mockDatastorePort).createDatastore(any(RewriteId.class));
  //
  //    RuntimeException thrown = assertThrows(RuntimeException.class, () -> datastoreManager.createADatastore(rewriteId));
  //    assertThat(thrown).isEqualTo(expectedException);
  //
  //    verify(mockDatastorePort, times(1)).createDatastore(any(RewriteId.class));
  //
  //    verifyNoInteractions(mockEventBusPort);
  //    verifyNoMoreInteractions(mockDatastorePort, mockRewriteConfig);
  //  }
  //
  //  @Test
  //  @DisplayName("createADatastore should handle RewriteId with specific UUID")
  //  void createADatastore_shouldHandleSpecificRewriteId() {
  //    RewriteId rewriteId =  RewriteId.from("/tmp/data");
  //    Path workDirectory = Path.of("/tmp/data");
  //    Path expectedDatastorePath = workDirectory.resolve(rewriteId.toString());
  //
  ////    when(mockRewriteConfig.workDirectory()).thenReturn(workDirectory);
  //
  //    datastoreManager.createADatastore(rewriteId);
  //
  //    verify(mockDatastorePort, times(1)).createDatastore(rewriteId);
  //    verify(mockEventBusPort, times(1)).publish(any(LoggingEvent.class));
  //    verifyNoMoreInteractions(mockDatastorePort, mockEventBusPort, mockRewriteConfig);
  //  }
  //
  //  @Test
  //  @DisplayName("createDatastore should save new state and create job when no existing state")
  //  void createDatastore_shouldSaveNewStateAndCreateJob_whenNoExistingState() {
  //    String repoUrl = "https://github.com/test/repo1.git";
  //
  //    RewriteId expectedRewriteId = RewriteId.from(repoUrl);
  //
  //    when(mockStateRepository.get(any(RewriteId.class))).thenReturn(Optional.empty());
  //
  //    RewriteId actualRewriteId = datastoreManager.createDatastore(RepositoryURL.from(repoUrl));
  //
  //    assertThat(actualRewriteId).isEqualTo(expectedRewriteId);
  //
  //    ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
  //    verify(mockStateRepository, times(1)).save(stateCaptor.capture());
  //    assertThat(stateCaptor.getValue().status()).isEqualTo(StateEnum.INIT);
  //    assertThat(stateCaptor.getValue().rewriteId()).isEqualTo(expectedRewriteId);
  //
  //    verify(mockJobPort, times(1)).createDatastoreJob(expectedRewriteId);
  //
  //
  //    verifyNoMoreInteractions(mockStateRepository, mockJobPort);
  //    verifyNoInteractions(mockEventBusPort);
  //  }
  //
  //  @Test
  //  @DisplayName("createDatastore should throw RewriteException when state already exists")
  //  void createDatastore_shouldThrowRewriteException_whenStateAlreadyExists() {
  //    String repoUrl = "https://github.com/test/existing-repo.git";
  //    RewriteId expectedRewriteId = RewriteId.from(repoUrl);
  //    Path existingDatastorePath = Path.of("/data/existing-datastore");
  //
  //    State existingState =
  //            new State(
  //                    expectedRewriteId,
  //                    StateEnum.REPO_CREATED,
  //                    Instant.now().minusSeconds(3600),
  //                    Instant.now()
  //            );
  //    when(mockStateRepository.get(any(RewriteId.class))).thenReturn(Optional.of(existingState));
  //
  //    Datastore existingDatastore =
  //            new Datastore(expectedRewriteId, existingDatastorePath, Collections.emptySet());
  //    when(mockDatastoreCreator.getDatastore(expectedRewriteId)).thenReturn(existingDatastore);
  //
  //    RewriteException thrownException = assertThrows(
  //            RewriteException.class,
  //            () -> datastoreManager.createDatastore(RepositoryURL.from(repoUrl))
  //    );
  //
  //    assertThat(thrownException.getMessage()).contains("Datastore already exists");
  //    assertThat(thrownException.getParameters())
  //            .containsEntry("id", expectedRewriteId.get().toString())
  //            .containsEntry("repositoryURL", repoUrl)
  //            .containsEntry("datastore.path", existingDatastorePath.toString());
  //
  //    verify(mockStateRepository, times(1)).get(expectedRewriteId);
  //    verify(mockDatastoreCreator, times(1)).getDatastore(expectedRewriteId);
  //
  //    verify(mockStateRepository, never()).save(any(State.class));
  //    verify(mockJobPort, never()).createDatastoreJob(any(RewriteId.class));
  //
  //    verifyNoMoreInteractions(mockStateRepository, mockDatastoreCreator);
  //    verifyNoInteractions(mockJobPort, mockEventBusPort);
  //  }
  //
  //  @Test
  //  @DisplayName("createDatastore should propagate DataAccessException from stateRepository.get")
  //  void createDatastore_shouldPropagateDataAccessException_fromGet() {
  //    String repoUrl = "https://github.com/test/error-repo.git";
  //    DataAccessException expectedException = new DataAccessException("Failed to get data");
  //
  //    when(mockStateRepository.get(any(RewriteId.class))).thenThrow(expectedException);
  //
  //    DataAccessException thrown = assertThrows(
  //            DataAccessException.class,
  //            () -> datastoreManager.createDatastore(RepositoryURL.from(repoUrl))
  //    );
  //    assertThat(thrown).isEqualTo(expectedException);
  //
  //    verify(mockStateRepository, times(1)).get(any(RewriteId.class));
  //    verifyNoInteractions(mockDatastoreCreator, mockJobPort, mockEventBusPort);
  //    verifyNoMoreInteractions(mockStateRepository);
  //  }
  //
  //  @Test
  //  @DisplayName("createDatastore should propagate DataAccessException from stateRepository.save")
  //  void createDatastore_shouldPropagateDataAccessException_fromSave() {
  //    String repoUrl = "https://github.com/test/save-error.git";
  //    DataAccessException expectedException = new DataAccessException("Failed to save data");
  //
  //    when(mockStateRepository.get(any(RewriteId.class))).thenReturn(Optional.empty());
  //    doThrow(expectedException).when(mockStateRepository).save(any(State.class));
  //
  //    DataAccessException thrown = assertThrows(
  //            DataAccessException.class,
  //            () -> datastoreManager.createDatastore(RepositoryURL.from(repoUrl))
  //    );
  //    assertThat(thrown).isEqualTo(expectedException);
  //
  //    verify(mockStateRepository, times(1)).get(any(RewriteId.class));
  //    verify(mockStateRepository, times(1)).save(any(State.class));
  //    verifyNoInteractions(mockDatastoreCreator, mockJobPort, mockEventBusPort);
  //    verifyNoMoreInteractions(mockStateRepository);
  //  }
  //
  //  @Test
  //  @DisplayName("createDatastore should propagate DataAccessException if jobPort.createDatastoreJob fails")
  //  void createDatastore_shouldPropagateDataAccessException_whenJobPortCreationFails() {
  //    String repoUrl = "https://github.com/test/job-error.git";
  //    RewriteId expectedRewriteId = RewriteId.from(repoUrl);
  //    DataAccessException expectedException = new DataAccessException("Failed to create job");
  //
  //    when(mockStateRepository.get(any(RewriteId.class))).thenReturn(Optional.empty());
  //    doThrow(expectedException).when(mockJobPort).createDatastoreJob(any(RewriteId.class));
  //
  //    DataAccessException thrown = assertThrows(
  //            DataAccessException.class,
  //            () -> datastoreManager.createDatastore(RepositoryURL.from(repoUrl))
  //    );
  //    assertThat(thrown).isEqualTo(expectedException);
  //
  //    verify(mockStateRepository, times(1)).get(expectedRewriteId);
  //    verify(mockStateRepository, times(1)).save(any(State.class));
  //    verify(mockJobPort, times(1)).createDatastoreJob(expectedRewriteId);
  //    verifyNoInteractions(mockDatastoreCreator, mockEventBusPort);
  //    verifyNoMoreInteractions(mockStateRepository, mockJobPort);
  //  }
  //
  //
  //  @Test
  //  @DisplayName("createDatastore should throw MissingMandatoryValueException if repoUrl is null")
  //  void createDatastore_shouldThrowMissingMandatoryValueException_whenRepoUrlIsNull() {
  //    String repoUrl = null;
  //    assertThrows(
  //            MissingMandatoryValueException.class,
  //            () -> datastoreManager.createDatastore(null)
  //    );
  //
  //    verifyNoInteractions(
  //            mockStateRepository,
  //            mockJobPort,
  //            mockDatastoreCreator,
  //            mockEventBusPort
  //    );
  //  }
  //
  //
  //
  //  @Test
  //  @DisplayName("createDatastore should return correct RewriteId for a given URL")
  //  void createDatastoreId() {
  //    String repoUrl = "https://github.com/myorg/my-specific-repo.git";
  //    RepositoryURL repositoryURL = RepositoryURL.from(repoUrl);
  //    RewriteId expectedRewriteId = RewriteId.from(repoUrl);
  //
  //    when(mockStateRepository.get(expectedRewriteId)).thenReturn(Optional.empty());
  //
  //    RewriteId actualRewriteId = datastoreManager.createDatastore(repositoryURL);
  //    assertThat(actualRewriteId).isEqualTo(expectedRewriteId);
  //
  //    verify(mockStateRepository).get(expectedRewriteId);
  //    verify(mockStateRepository).save(any(State.class));
  //    verify(mockJobPort).createDatastoreJob(expectedRewriteId);
  //
  //    verifyNoInteractions(mockDatastoreCreator);
  //
  //    verifyNoMoreInteractions(mockStateRepository, mockJobPort, mockEventBusPort);
  //  }
}
