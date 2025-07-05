package fr.rewrite.server.domain.datastore;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.shared.error.domain.ErrorStatus;
import org.junit.jupiter.api.Test;

@UnitTest
class DatastoreOperationExceptionTest {

  @Test
  void shouldCreateExceptionWithMessage() {
    String message = "Test message";
    DatastoreOperationException exception = new DatastoreOperationException(message);

    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.key()).isEqualTo(DatastoreErrorKey.DATASTORE_OPERATION_ERROR);
    assertThat(exception.status()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void shouldCreateExceptionWithMessageAndCause() {
    String message = "Test message with cause";
    Throwable cause = new RuntimeException("Original cause");
    DatastoreOperationException exception = new DatastoreOperationException(message, cause);

    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.getCause()).isEqualTo(cause);
    assertThat(exception.key()).isEqualTo(DatastoreErrorKey.DATASTORE_OPERATION_ERROR);
    assertThat(exception.status()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR);
  }
}
