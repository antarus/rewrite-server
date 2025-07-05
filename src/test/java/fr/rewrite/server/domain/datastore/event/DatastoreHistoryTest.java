package fr.rewrite.server.domain.datastore.event;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.repository.RepositoryURL;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class DatastoreHistoryTest {

  private final DatastoreId TEST_DATASTORE_ID = new DatastoreId(UUID.randomUUID());
  private final RepositoryURL TEST_REPO_URL = new RepositoryURL("https://github.com/test/repo");

  @Test
  void shouldCreateDatastoreHistoryWithVarargs() {
    DatastoreCreated startEvent = DatastoreCreated.from(TEST_REPO_URL);
    DatastoreDeleted deletedEvent = DatastoreDeleted.from(TEST_DATASTORE_ID, new SequenceId(10));

    DatastoreHistory history = new DatastoreHistory(startEvent, deletedEvent);

    assertThat(history.start()).isEqualTo(startEvent);
    assertThat(history.followingEvents()).containsExactly(deletedEvent);
  }

  @Test
  void shouldReturnCorrectHistoryStream() {
    DatastoreCreated startEvent = DatastoreCreated.from(TEST_REPO_URL);
    DatastoreDeleted deletedEvent = DatastoreDeleted.from(TEST_DATASTORE_ID, new SequenceId(10));

    DatastoreHistory history = new DatastoreHistory(startEvent, deletedEvent);

    assertThat(history.historyStream()).containsExactly(startEvent, deletedEvent);
  }

  @Test
  void shouldReturnLastSequenceId() {
    DatastoreCreated startEvent = DatastoreCreated.from(TEST_REPO_URL);
    DatastoreDeleted deletedEvent = DatastoreDeleted.from(TEST_DATASTORE_ID, new SequenceId(10));
    DatastoreStatusChanged statusChangedEvent = DatastoreStatusChanged.from(
      TEST_DATASTORE_ID,
      fr.rewrite.server.domain.StatusEnum.BUILDING,
      new SequenceId(20)
    );

    DatastoreHistory history = new DatastoreHistory(startEvent, deletedEvent, statusChangedEvent);

    assertThat(history.lastSequenceId()).isEqualTo(new SequenceId(20));
  }

  @Test
  void shouldReturnInitialSequenceIdWhenOnlyStartEvent() {
    DatastoreCreated startEvent = DatastoreCreated.from(TEST_REPO_URL);

    DatastoreHistory history = new DatastoreHistory(startEvent);

    assertThat(history.lastSequenceId()).isEqualTo(SequenceId.INITIAL);
  }
}
