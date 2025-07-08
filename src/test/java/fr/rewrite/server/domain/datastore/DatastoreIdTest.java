package fr.rewrite.server.domain.datastore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class DatastoreIdTest {

  @Test
  void shouldCreateDatastoreIdFromRepositoryURL() {
    RepositoryURL repositoryURL = new RepositoryURL("https://github.com/test/repo");
    DatastoreId datastoreId = DatastoreId.from(repositoryURL);
    assertThat(datastoreId.get()).isEqualTo(UUID.nameUUIDFromBytes(repositoryURL.url().getBytes()));
  }

  @Test
  void shouldThrowExceptionWhenUUIDIsNull() {
    assertThatThrownBy(() -> new DatastoreId(null)).isInstanceOf(MissingMandatoryValueException.class).hasMessageContaining("uuid");
  }

  @Test
  void shouldThrowExceptionWhenRepositoryURLIsNull() {
    assertThatThrownBy(() -> DatastoreId.from((RepositoryURL) null))
      .isInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("repositoryURL");
  }
}
