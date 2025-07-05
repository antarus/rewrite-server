package fr.rewrite.server.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.DatastoreId;
import org.junit.jupiter.api.Test;

@UnitTest
class CloneRepositoryExceptionTest {

  @Test
  void shouldCreateExceptionWithCorrectParameters() {
    RepositoryURL repositoryURL = new RepositoryURL("https://github.com/test/repo");
    DatastoreId expectedDatastoreId = DatastoreId.from(repositoryURL);
    Exception cause = new RuntimeException("Test cause");

    CloneRepositoryException exception = new CloneRepositoryException(repositoryURL, cause);

    assertThat(exception.key()).isEqualTo(RepositoryErrorKey.REPOSITORY_CLONE_ERROR);
    assertThat(exception.parameters().get("id")).isEqualTo(expectedDatastoreId.get().toString());
    assertThat(exception.parameters().get("repository.url")).isEqualTo(repositoryURL.toString());
    assertThat(exception.getCause()).isEqualTo(cause);
  }
}
