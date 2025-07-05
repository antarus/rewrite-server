package fr.rewrite.server.domain.datastore.projections.currentstate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.StatusEnum;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.datastore.event.DatastoreCreateErrorEvent;
import fr.rewrite.server.domain.datastore.event.DatastoreCreated;
import fr.rewrite.server.domain.datastore.event.DatastoreDeleted;
import fr.rewrite.server.domain.datastore.event.DatastoreStatusChanged;
import fr.rewrite.server.domain.repository.RepositoryURL;
import org.junit.jupiter.api.Test;

@UnitTest
class DatastoreCurrentStateTest {

  private final RepositoryURL TEST_REPO_URL = new RepositoryURL("https://github.com/test/repo");

  private final DatastoreId TEST_DATASTORE_ID = DatastoreId.from(TEST_REPO_URL);

  @Test
  void fromDatastoreCreatedShouldReturnCorrectState() {
    DatastoreCreated createdEvent = DatastoreCreated.from(TEST_REPO_URL);
    DatastoreCurrentState state = DatastoreCurrentState.from(createdEvent);

    assertThat(state.datastoreId()).isEqualTo(TEST_DATASTORE_ID);
    assertThat(state.repositoryURL()).isEqualTo(TEST_REPO_URL);
    assertThat(state.lastStatus()).isEqualTo(StatusEnum.DATASTORE_CREATED);
    assertThat(state.currentSequenceId()).isEqualTo(SequenceId.INITIAL);
    assertThat(state.deleted()).isFalse();
  }

  @Test
  void applyDatastoreDeletedEventShouldMarkAsDeleted() {
    DatastoreCurrentState initialState = new DatastoreCurrentState(
      TEST_DATASTORE_ID,
      TEST_REPO_URL,
      StatusEnum.DATASTORE_CREATED,
      new SequenceId(0),
      false
    );
    SequenceId nextSequence = new SequenceId(10);
    DatastoreDeleted deletedEvent = DatastoreDeleted.from(TEST_DATASTORE_ID, nextSequence);

    DatastoreCurrentState newState = initialState.apply(deletedEvent);

    assertThat(newState.deleted()).isTrue();
    assertThat(newState.currentSequenceId()).isEqualTo(nextSequence);
  }

  @Test
  void applyDatastoreCreateErrorEventShouldUpdateSequenceId() {
    DatastoreCurrentState initialState = new DatastoreCurrentState(
      TEST_DATASTORE_ID,
      TEST_REPO_URL,
      StatusEnum.DATASTORE_CREATED,
      new SequenceId(0),
      false
    );
    SequenceId nextSequence = new SequenceId(20);
    DatastoreCreateErrorEvent errorEvent = DatastoreCreateErrorEvent.from(
      TEST_DATASTORE_ID,
      TEST_REPO_URL,
      nextSequence,
      new RuntimeException("Test error")
    );

    DatastoreCurrentState newState = initialState.apply(errorEvent);

    assertThat(newState.lastStatus()).isEqualTo(StatusEnum.DATASTORE_CREATED);
    assertThat(newState.currentSequenceId()).isEqualTo(nextSequence);
  }

  @Test
  void applyDatastoreCreatedEventShouldThrowException() {
    DatastoreCurrentState initialState = new DatastoreCurrentState(
      TEST_DATASTORE_ID,
      TEST_REPO_URL,
      StatusEnum.DATASTORE_CREATED,
      new SequenceId(0),
      false
    );
    DatastoreCreated createdEvent = DatastoreCreated.from(TEST_REPO_URL);

    assertThatThrownBy(() -> initialState.apply(createdEvent))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("DatastoreCreated event is not expected as an update event");
  }

  @Test
  void applyDatastoreStatusChangedEventShouldUpdateStatusAndSequenceId() {
    DatastoreCurrentState initialState = new DatastoreCurrentState(
      TEST_DATASTORE_ID,
      TEST_REPO_URL,
      StatusEnum.DATASTORE_CREATED,
      new SequenceId(0),
      false
    );
    SequenceId nextSequence = new SequenceId(30);
    DatastoreStatusChanged statusChangedEvent = DatastoreStatusChanged.from(TEST_DATASTORE_ID, StatusEnum.REPOSITORY_CLONING, nextSequence);

    DatastoreCurrentState newState = initialState.apply(statusChangedEvent);

    assertThat(newState.lastStatus()).isEqualTo(StatusEnum.REPOSITORY_CLONING);
    assertThat(newState.currentSequenceId()).isEqualTo(nextSequence);
  }

  @Test
  void toDomainShouldReturnDatastoreWithCurrentState() {
    DatastoreCurrentState initialState = new DatastoreCurrentState(
      TEST_DATASTORE_ID,
      TEST_REPO_URL,
      StatusEnum.DATASTORE_CREATED,
      new SequenceId(0),
      false
    );

    assertThat(initialState.toDomain().datastoreId()).isEqualTo(TEST_DATASTORE_ID);
    assertThat(initialState.toDomain().repositoryURL()).isEqualTo(TEST_REPO_URL);
    assertThat(initialState.toDomain().state()).isEqualTo(StatusEnum.DATASTORE_CREATED);
  }
}
