package fr.rewrite.server.domain.datastore;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.repository.RepositoryURL;
import org.junit.jupiter.api.Test;

@UnitTest
class DatastoreAlreadyExistExceptionTest {

  @Test
  void shouldCreateExceptionWithCorrectParameters() {
    RepositoryURL repositoryURL = new RepositoryURL("https://github.com/test/repo");
    DatastoreId expectedDatastoreId = DatastoreId.from(repositoryURL);

    DatastoreAlreadyExistException exception = new DatastoreAlreadyExistException(repositoryURL);

    assertThat(exception.key()).isEqualTo(DatastoreErrorKey.DATASTORE_ALREADY_EXIST);
    assertThat(exception.parameters().get("id")).isEqualTo(expectedDatastoreId.get().toString());
    assertThat(exception.parameters().get("repository.url")).isEqualTo(repositoryURL.url());
  }
}
