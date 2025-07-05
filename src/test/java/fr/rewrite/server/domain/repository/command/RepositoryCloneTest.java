package fr.rewrite.server.domain.repository.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.repository.Credentials;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class RepositoryCloneTest {

  private final DatastoreId TEST_DATASTORE_ID = new DatastoreId(UUID.randomUUID());
  private final RepositoryURL TEST_REPO_URL = new RepositoryURL("https://github.com/test/repo");

  @Test
  void shouldCreateRepositoryCloneCommandWithoutCredentials() {
    RepositoryClone command = RepositoryClone.from(TEST_DATASTORE_ID, TEST_REPO_URL);

    assertThat(command.datastoreId()).isEqualTo(TEST_DATASTORE_ID);
    assertThat(command.repositoryURL()).isEqualTo(TEST_REPO_URL);
    assertThat(command.credential()).isEmpty();
  }

  @Test
  void shouldCreateRepositoryCloneCommandWithCredentials() {
    Credentials credentials = new Credentials("user", "pat");
    RepositoryClone command = RepositoryClone.from(TEST_DATASTORE_ID, TEST_REPO_URL, credentials);

    assertThat(command.datastoreId()).isEqualTo(TEST_DATASTORE_ID);
    assertThat(command.repositoryURL()).isEqualTo(TEST_REPO_URL);
    assertThat(command.credential()).contains(credentials);
  }

  @Test
  void shouldCreateRepositoryCloneCommandWithNullCredentials() {
    RepositoryClone command = RepositoryClone.from(TEST_DATASTORE_ID, TEST_REPO_URL, null);

    assertThat(command.datastoreId()).isEqualTo(TEST_DATASTORE_ID);
    assertThat(command.repositoryURL()).isEqualTo(TEST_REPO_URL);
    assertThat(command.credential()).isEmpty();
  }

  @Test
  void shouldThrowExceptionWhenDatastoreIdIsNull() {
    assertThatThrownBy(() -> new RepositoryClone(null, TEST_REPO_URL, Optional.empty()))
      .isInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("datastoreId");
  }

  @Test
  void shouldThrowExceptionWhenRepositoryURLIsNull() {
    assertThatThrownBy(() -> new RepositoryClone(TEST_DATASTORE_ID, null, Optional.empty()))
      .isInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("repositoryURL");
  }

  @Test
  void shouldThrowExceptionWhenCredentialOptionalIsNull() {
    assertThatThrownBy(() -> new RepositoryClone(TEST_DATASTORE_ID, TEST_REPO_URL, null))
      .isInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("credential");
  }
}
