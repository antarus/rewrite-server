package fr.rewrite.server.domain.datastore.event;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.repository.RepositoryURL;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class DatastoreCreateErrorEventTest {

  @Test
  void shouldCreateDatastoreCreateErrorEvent() {
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    RepositoryURL repositoryURL = new RepositoryURL("https://github.com/test/repo");
    SequenceId sequenceId = new SequenceId(1);
    Throwable throwable = new RuntimeException("Test Exception");

    DatastoreCreateErrorEvent event = DatastoreCreateErrorEvent.from(datastoreId, repositoryURL, sequenceId, throwable);

    assertThat(event.datastoreId()).isEqualTo(datastoreId);
    assertThat(event.repositoryURL()).isEqualTo(repositoryURL);
    assertThat(event.sequenceId()).isEqualTo(sequenceId);
    assertThat(event.throwable()).isEqualTo(throwable);
    assertThat(event.eventId()).isNotNull();
    assertThat(event.occurredOn()).isNotNull();
  }
}
