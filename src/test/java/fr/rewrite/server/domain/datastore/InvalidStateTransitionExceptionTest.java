package fr.rewrite.server.domain.datastore;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.StatusEnum;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class InvalidStateTransitionExceptionTest {

  @Test
  void shouldCreateExceptionWithCorrectParameters() {
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    StatusEnum fromStatus = StatusEnum.INIT;
    StatusEnum toStatus = StatusEnum.BUILDING;

    InvalidStateTransitionException exception = new InvalidStateTransitionException(datastoreId, fromStatus, toStatus);

    assertThat(exception.getMessage()).contains("Invalid state transition from INIT to BUILDING");
    assertThat(exception.parameters().get("id")).isEqualTo(datastoreId.get().toString());
    assertThat(exception.parameters().get("from")).isEqualTo(fromStatus.toString());
    assertThat(exception.parameters().get("to")).isEqualTo(toStatus.toString());
    assertThat(exception.key()).isEqualTo(DatastoreErrorKey.INVALID_STATE_TRANSITION);
  }
}
