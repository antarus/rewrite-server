package fr.rewrite.server.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.DatastoreId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class CreateBranchExceptionTest {

  @Test
  void shouldCreateExceptionWithCorrectParameters() {
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    String branchName = "feature/test";
    Exception cause = new RuntimeException("Test cause");

    CreateBranchException exception = new CreateBranchException(datastoreId, branchName, cause);

    assertThat(exception.key()).isEqualTo(RepositoryErrorKey.BRANCH_CREATION_ERROR);
    assertThat(exception.parameters().get("id")).isEqualTo(datastoreId.get().toString());
    assertThat(exception.parameters().get("branchName")).isEqualTo(branchName);
    assertThat(exception.getCause()).isEqualTo(cause);
  }
}
