package fr.rewrite.server.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.events.LoggingEvent;
import fr.rewrite.server.domain.spi.DatastorePort;
import fr.rewrite.server.domain.spi.EventBusPort;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DatastoreCreatorTest {

  @Mock
  private RewriteConfig mockRewriteConfig;

  @Mock
  private DatastorePort mockDatastorePort;

  @Mock
  private EventBusPort mockEventBusPort;

  private DatastoreCreator datastoreCreator;

  @BeforeEach
  void setUp() {
    datastoreCreator = new DatastoreCreator(mockRewriteConfig, mockDatastorePort, mockEventBusPort);
  }

  @Test
  @DisplayName("Constructor should throw MissingMandatoryValueException when rewriteConfig is null")
  void constructor_shouldThrowExceptionWhenRewriteConfigIsNull() {
    assertThrows(MissingMandatoryValueException.class, () -> new DatastoreCreator(null, mockDatastorePort, mockEventBusPort));
  }

  @Test
  @DisplayName("Constructor should throw MissingMandatoryValueException when datastorePort is null")
  void constructor_shouldThrowExceptionWhenDatastorePortIsNull() {
    assertThrows(MissingMandatoryValueException.class, () -> new DatastoreCreator(mockRewriteConfig, null, mockEventBusPort));
  }

  @Test
  @DisplayName("Constructor should throw MissingMandatoryValueException when eventBus is null")
  void constructor_shouldThrowExceptionWhenEventBusIsNull() {
    assertThrows(MissingMandatoryValueException.class, () -> new DatastoreCreator(mockRewriteConfig, mockDatastorePort, null));
  }

  @Test
  @DisplayName("createADatastore should create datastore and publish logging event")
  void createADatastore_shouldCreateDatastoreAndPublishEvent() {
    RewriteId rewriteId = new RewriteId(UUID.randomUUID());
    Path workDirectory = Path.of("/app/temp/work");
    Path expectedDatastorePath = workDirectory.resolve(rewriteId.get().toString());

    when(mockRewriteConfig.workDirectory()).thenReturn(workDirectory);

    datastoreCreator.createADatastore(rewriteId);

    ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
    verify(mockDatastorePort, times(1)).createDatastore(pathCaptor.capture());
    assertThat(pathCaptor.getValue()).isEqualTo(expectedDatastorePath);

    ArgumentCaptor<LoggingEvent> eventCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
    verify(mockEventBusPort, times(1)).publish(eventCaptor.capture());
    assertThat(eventCaptor.getValue().log()).isEqualTo("createADatastore");

    verify(mockRewriteConfig, times(1)).workDirectory();
    verifyNoMoreInteractions(mockDatastorePort, mockEventBusPort, mockRewriteConfig);
  }

  @Test
  @DisplayName("createADatastore should propagate RuntimeException from datastorePort.createDatastore")
  void createADatastore_shouldPropagateException_fromDatastorePort() {
    RewriteId rewriteId = new RewriteId(UUID.randomUUID());
    Path workDirectory = Path.of("/app/temp/work");
    RuntimeException expectedException = new RuntimeException("Datastore creation failed!");

    when(mockRewriteConfig.workDirectory()).thenReturn(workDirectory);
    doThrow(expectedException).when(mockDatastorePort).createDatastore(any(Path.class));

    RuntimeException thrown = assertThrows(RuntimeException.class, () -> datastoreCreator.createADatastore(rewriteId));
    assertThat(thrown).isEqualTo(expectedException);

    verify(mockDatastorePort, times(1)).createDatastore(any(Path.class));
    verify(mockRewriteConfig, times(1)).workDirectory();
    verifyNoInteractions(mockEventBusPort);
    verifyNoMoreInteractions(mockDatastorePort, mockRewriteConfig);
  }

  @Test
  @DisplayName("createADatastore should handle RewriteId with specific UUID")
  void createADatastore_shouldHandleSpecificRewriteId() {
    UUID specificUuid = UUID.fromString("12345678-1234-1234-1234-1234567890ab");
    RewriteId rewriteId = new RewriteId(specificUuid);
    Path workDirectory = Path.of("/mnt/data");
    Path expectedDatastorePath = workDirectory.resolve(specificUuid.toString());

    when(mockRewriteConfig.workDirectory()).thenReturn(workDirectory);

    datastoreCreator.createADatastore(rewriteId);

    verify(mockDatastorePort, times(1)).createDatastore(expectedDatastorePath);
    verify(mockEventBusPort, times(1)).publish(any(LoggingEvent.class));
    verifyNoMoreInteractions(mockDatastorePort, mockEventBusPort, mockRewriteConfig);
  }
}
