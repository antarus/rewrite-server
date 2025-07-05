package fr.rewrite.server.domain.datastore.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import org.junit.jupiter.api.Test;

@UnitTest
class DatastoreCreationTest {

  @Test
  void shouldCreateDatastoreCreationCommand() {
    RepositoryURL repositoryURL = new RepositoryURL("https://github.com/test/repo");
    DatastoreCreation command = new DatastoreCreation(repositoryURL);

    assertThat(command.repositoryURL()).isEqualTo(repositoryURL);
    assertThat(command.datastoreId()).isEqualTo(DatastoreId.from(repositoryURL));
  }

  @Test
  void shouldThrowExceptionWhenRepositoryURLIsNull() {
    assertThatThrownBy(() -> new DatastoreCreation(null))
      .isInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("repositoryURL");
  }
}
