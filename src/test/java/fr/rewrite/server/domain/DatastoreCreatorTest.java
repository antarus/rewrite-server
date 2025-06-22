package fr.rewrite.server.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.DatastorePort;
import fr.rewrite.server.domain.spi.EventBusPort;
import fr.rewrite.server.domain.state.RewriteConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
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

  //  private DatastoreCreator datastoreWorker;

  @BeforeEach
  void setUp() {
    //    datastoreWorker = new DatastoreCreator(mockRewriteConfig, mockDatastorePort, mockEventBusPort);
  }
}
