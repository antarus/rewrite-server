package fr.rewrite.server.domain.repository.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class RepositoryDeleteTest {

  @Test
  void shouldCreateRepositoryDeleteCommand() {
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    RepositoryDelete command = new RepositoryDelete(datastoreId);

    assertThat(command.datastoreId()).isEqualTo(datastoreId);
  }

  @Test
  void shouldThrowExceptionWhenDatastoreIdIsNull() {
    assertThatThrownBy(() -> new RepositoryDelete(null))
      .isInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("datastoreId");
  }
}
