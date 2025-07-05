package fr.rewrite.server.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class RepositoryErrorKeyTest {

  @Test
  void shouldReturnCorrectKeyForRepositoryInvalidUri() {
    assertThat(RepositoryErrorKey.REPOSITORY_INVALID_URI.get()).isEqualTo("repository-invalid-uri");
  }

  @Test
  void shouldReturnCorrectKeyForRepositoryCloneError() {
    assertThat(RepositoryErrorKey.REPOSITORY_CLONE_ERROR.get()).isEqualTo("repository-clone-error");
  }

  @Test
  void shouldReturnCorrectKeyForBranchCreationError() {
    assertThat(RepositoryErrorKey.BRANCH_CREATION_ERROR.get()).isEqualTo("branch-creation-error");
  }
}
