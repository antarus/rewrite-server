package fr.rewrite.server.domain.datastore;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class DatastoreErrorKeyTest {

  @Test
  void shouldReturnCorrectKeyForDatastoreOperationError() {
    assertThat(DatastoreErrorKey.DATASTORE_OPERATION_ERROR.get()).isEqualTo("datastore-operation-error");
  }

  @Test
  void shouldReturnCorrectKeyForDatastoreAlreadyExist() {
    assertThat(DatastoreErrorKey.DATASTORE_ALREADY_EXIST.get()).isEqualTo("datastore-already-exist");
  }

  @Test
  void shouldReturnCorrectKeyForDatastoreNotFound() {
    assertThat(DatastoreErrorKey.DATASTORE_NOT_FOUND.get()).isEqualTo("datastore-not-found");
  }

  @Test
  void shouldReturnCorrectKeyForDatastoreNotValid() {
    assertThat(DatastoreErrorKey.DATASTORE_NOT_VALID.get()).isEqualTo("datastore-not-valid");
  }

  @Test
  void shouldReturnCorrectKeyForInvalidStateTransition() {
    assertThat(DatastoreErrorKey.INVALID_STATE_TRANSITION.get()).isEqualTo("invalid-state-transition");
  }
}
