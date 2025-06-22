package fr.rewrite.server.domain.datastore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.events.LoggingEvent;
import fr.rewrite.server.domain.spi.EventBusPort;
import java.nio.file.Path;
import java.util.Collections;
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
class DatastoreWorkerTest {

  @Mock
  private DatastorePort mockDatastorePort;

  @Mock
  private EventBusPort mockEventBusPort;

  private DatastoreWorker datastoreWorker;

  @BeforeEach
  void setUp() {
    datastoreWorker = new DatastoreWorker(mockDatastorePort, mockEventBusPort);
  }

  @Test
  @DisplayName("createADatastore should call datastorePort.createDatastore and publish a LoggingEvent")
  void createADatastore_shouldCreateDatastoreAndPublishEvent() {
    RewriteId testRewriteId = new RewriteId(UUID.randomUUID());

    datastoreWorker.createADatastore(testRewriteId);
    verify(mockDatastorePort, times(1)).createDatastore(testRewriteId);

    ArgumentCaptor<DatastoreCreatedEvent> eventCaptor = ArgumentCaptor.forClass(DatastoreCreatedEvent.class);
    verify(mockEventBusPort, times(1)).publish(eventCaptor.capture());
    assertThat(eventCaptor.getValue().rewriteId()).isEqualTo(testRewriteId);

    verifyNoMoreInteractions(mockDatastorePort, mockEventBusPort);
  }

  @Test
  @DisplayName("createADatastore should propagate exceptions from datastorePort.createDatastore")
  void createADatastore_shouldPropagateExceptionFromDatastorePort() {
    RewriteId testRewriteId = new RewriteId(UUID.randomUUID());
    RuntimeException expectedException = new RuntimeException("Datastore creation failed!");

    doThrow(expectedException).when(mockDatastorePort).createDatastore(any(RewriteId.class));

    RuntimeException thrown = assertThrows(RuntimeException.class, () -> datastoreWorker.createADatastore(testRewriteId));
    assertThat(thrown).isEqualTo(expectedException);

    verify(mockDatastorePort, times(1)).createDatastore(testRewriteId);
    verifyNoInteractions(mockEventBusPort); // No event if creation failed
    verifyNoMoreInteractions(mockDatastorePort);
  }

  @Test
  @DisplayName("createADatastore should propagate exceptions from eventBusPort.publish")
  void createADatastore_shouldPropagateExceptionFromEventBusPort() {
    RewriteId testRewriteId = new RewriteId(UUID.randomUUID());
    RuntimeException expectedException = new RuntimeException("Event publishing failed!");

    doThrow(expectedException).when(mockEventBusPort).publish(any(DatastoreCreatedEvent.class));

    RuntimeException thrown = assertThrows(RuntimeException.class, () -> datastoreWorker.createADatastore(testRewriteId));
    assertThat(thrown).isEqualTo(expectedException);

    verify(mockDatastorePort, times(1)).createDatastore(testRewriteId);
    verify(mockEventBusPort, times(1)).publish(any(DatastoreCreatedEvent.class));
    verifyNoMoreInteractions(mockDatastorePort, mockEventBusPort);
  }

  @Test
  @DisplayName("getDatastore should call datastorePort.getDatastore and return the result")
  void getDatastore_shouldCallGetDatastoreAndReturnResult() {
    RewriteId testRewriteId = new RewriteId(UUID.randomUUID());
    Datastore expectedDatastore = new Datastore(testRewriteId, Path.of("/repo/path"), Collections.emptySet());

    when(mockDatastorePort.getDatastore(testRewriteId)).thenReturn(expectedDatastore);

    Datastore actualDatastore = datastoreWorker.getDatastore(testRewriteId);

    assertThat(actualDatastore).isEqualTo(expectedDatastore);

    verify(mockDatastorePort, times(1)).getDatastore(testRewriteId);

    verifyNoInteractions(mockEventBusPort);
    verifyNoMoreInteractions(mockDatastorePort);
  }

  @Test
  @DisplayName("getDatastore should propagate exceptions from datastorePort.getDatastore")
  void getDatastore_shouldPropagateExceptionFromDatastorePort() {
    RewriteId testRewriteId = new RewriteId(UUID.randomUUID());
    RuntimeException expectedException = new RuntimeException("Datastore retrieval failed!");

    doThrow(expectedException).when(mockDatastorePort).getDatastore(any(RewriteId.class));

    RuntimeException thrown = assertThrows(RuntimeException.class, () -> datastoreWorker.getDatastore(testRewriteId));
    assertThat(thrown).isEqualTo(expectedException);

    verify(mockDatastorePort, times(1)).getDatastore(testRewriteId);
    verifyNoInteractions(mockEventBusPort);
    verifyNoMoreInteractions(mockDatastorePort);
  }
}
