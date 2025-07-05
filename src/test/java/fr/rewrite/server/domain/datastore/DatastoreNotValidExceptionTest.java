package fr.rewrite.server.domain.datastore;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class DatastoreNotValidExceptionTest {

  @Test
  void shouldCreateExceptionWithCorrectParameters() {
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());

    DatastoreNotValidException exception = new DatastoreNotValidException(datastoreId);

    assertThat(exception.key()).isEqualTo(DatastoreErrorKey.DATASTORE_NOT_VALID);
    assertThat(exception.parameters().get("id")).isEqualTo(datastoreId.get().toString());
  }
}
