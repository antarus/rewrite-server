package fr.rewrite.server.domain.repository.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.repository.RepositoryBranchName;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class RepositoryBranchCreateTest {

  private final DatastoreId TEST_DATASTORE_ID = new DatastoreId(UUID.randomUUID());
  private final RepositoryBranchName TEST_BRANCH_NAME = new RepositoryBranchName("main");

  @Test
  void shouldCreateRepositoryBranchCreateCommand() {
    RepositoryBranchCreate command = new RepositoryBranchCreate(TEST_DATASTORE_ID, TEST_BRANCH_NAME);

    assertThat(command.datastoreId()).isEqualTo(TEST_DATASTORE_ID);
    assertThat(command.branchName()).isEqualTo(TEST_BRANCH_NAME);
  }

  @Test
  void shouldCreateRepositoryBranchCreateCommandFromStaticMethod() {
    RepositoryBranchCreate command = RepositoryBranchCreate.from(TEST_DATASTORE_ID, TEST_BRANCH_NAME);

    assertThat(command.datastoreId()).isEqualTo(TEST_DATASTORE_ID);
    assertThat(command.branchName()).isEqualTo(TEST_BRANCH_NAME);
  }

  @Test
  void shouldThrowExceptionWhenDatastoreIdIsNull() {
    assertThatThrownBy(() -> new RepositoryBranchCreate(null, TEST_BRANCH_NAME))
      .isInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("datastoreId");
  }

  @Test
  void shouldThrowExceptionWhenBranchNameIsNull() {
    assertThatThrownBy(() -> new RepositoryBranchCreate(TEST_DATASTORE_ID, null))
      .isInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("branchName");
  }
}
