package fr.rewrite.server.domain.datastore.event;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.datastore.DatastoreId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class DatastoreDeletedTest {

  @Test
  void shouldCreateDatastoreDeletedEvent() {
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    SequenceId sequenceId = new SequenceId(1);

    DatastoreDeleted event = DatastoreDeleted.from(datastoreId, sequenceId);

    assertThat(event.datastoreId()).isEqualTo(datastoreId);
    assertThat(event.sequenceId()).isEqualTo(sequenceId);
    assertThat(event.eventId()).isNotNull();
    assertThat(event.occurredOn()).isNotNull();
  }
}
