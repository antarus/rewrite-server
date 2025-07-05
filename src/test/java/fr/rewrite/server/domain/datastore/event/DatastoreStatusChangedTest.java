package fr.rewrite.server.domain.datastore.event;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.StatusEnum;
import fr.rewrite.server.domain.datastore.DatastoreId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class DatastoreStatusChangedTest {

  @Test
  void shouldCreateDatastoreStatusChangedEvent() {
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    StatusEnum newStatus = StatusEnum.BUILDING;
    SequenceId sequenceId = new SequenceId(1);

    DatastoreStatusChanged event = DatastoreStatusChanged.from(datastoreId, newStatus, sequenceId);

    assertThat(event.datastoreId()).isEqualTo(datastoreId);
    assertThat(event.newStatus()).isEqualTo(newStatus);
    assertThat(event.sequenceId()).isEqualTo(sequenceId);
    assertThat(event.eventId()).isNotNull();
    assertThat(event.occurredOn()).isNotNull();
  }
}
