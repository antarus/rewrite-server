package fr.rewrite.server.domain.datastore.event;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.repository.RepositoryURL;
import org.junit.jupiter.api.Test;

@UnitTest
class DatastoreCreatedTest {

  @Test
  void shouldCreateDatastoreCreatedEvent() {
    RepositoryURL repositoryURL = new RepositoryURL("https://github.com/test/repo");
    DatastoreId expectedDatastoreId = DatastoreId.from(repositoryURL);

    DatastoreCreated event = DatastoreCreated.from(repositoryURL);

    assertThat(event.datastoreId()).isEqualTo(expectedDatastoreId);
    assertThat(event.repositoryURL()).isEqualTo(repositoryURL);
    assertThat(event.sequenceId()).isEqualTo(SequenceId.INITIAL);
    assertThat(event.eventId()).isNotNull();
    assertThat(event.occurredOn()).isNotNull();
  }
}
