package fr.rewrite.server.domain.repository;

import static org.junit.jupiter.api.Assertions.*;

import fr.rewrite.server.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@UnitTest
class RepositoryBranchNameTest {

  @ParameterizedTest
  @ValueSource(
    strings = {
      "main",
      "develop",
      "feature/my-new-feature",
      "bugfix/issue-123",
      "hotfix/critical_bug",
      "release/1.0.0",
      "branch-with-hyphens",
      "branch_with_underscores",
      "branch.with.dots",
      "my/nested/branch/name",
      "123-feature",
      "a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z",
      "feature/branch-name-with-numbers-123",
      "HEAD-not-the-exact-word",
    }
  )
  @DisplayName("Should create RepositoryBranchName for valid branch names")
  void shouldCreateRepositoryBranchNameForValidNames(String branchName) {
    assertDoesNotThrow(() -> new RepositoryBranchName(branchName));
    assertEquals(branchName, new RepositoryBranchName(branchName).name());
  }

  @ParameterizedTest
  @ValueSource(
    strings = {
      "/starts-with-slash",
      "ends-with-slash/",
      "ends-with-dot.",
      "contains//double-slash",
      "contains..double-dot",
      "my branch with spaces",
      "branch~with~tilde",
      "branch^with^caret",
      "branch?with?question",
      "branch*with*asterisk",
      "branch(with)paren",
      "branch)with)paren",
      "branch[with[bracket",
      "branch]with]bracket",
      "branch{with{brace",
      "branch}with}brace",
      "branch@with@at",
      "branch:with:colon",
      "branch;with;semicolon",
      "branch\\with\\backslash",
      "mybranch.lock",
      "HEAD",
      "feature/@{invalid",
      "feature/@.invalid",
      ".git/some-path",
      "invalid#char",
      "invalid&char",
    }
  )
  @DisplayName("Should throw IllegalArgumentException for invalid branch names on construction")
  void shouldThrowExceptionForInvalidNamesOnConstruction(String branchName) {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> new RepositoryBranchName(branchName));
    assertTrue(thrown.getMessage().contains("is invalid"));
  }
}
