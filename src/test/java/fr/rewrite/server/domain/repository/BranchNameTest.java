package fr.rewrite.server.domain.repository;

import static org.junit.jupiter.api.Assertions.*;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@UnitTest
class BranchNameTest {

  @Test
  @DisplayName("Should create BranchName with a valid name")
  void shouldCreateBranchNameWithValidName() {
    String name = "main";
    BranchName branchName = new BranchName(name);
    assertNotNull(branchName);
    assertEquals(name, branchName.get());
  }

  @Test
  @DisplayName("Should create BranchName with a complex branch name")
  void shouldCreateBranchNameWithComplexName() {
    String name = "feature/add-new-module_v2.0";
    BranchName branchName = new BranchName(name);
    assertNotNull(branchName);
    assertEquals(name, branchName.get());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { " ", "\t", "\n" })
  @DisplayName("Should throw IllegalArgumentException for null or empty branch names")
  void shouldThrowExceptionForNullOrEmptyBranchNames(String invalidName) {
    assertThrows(MissingMandatoryValueException.class, () -> new BranchName(invalidName));
  }

  @Test
  @DisplayName("Should test BranchName equality based on value")
  void shouldTestEqualityBasedOnValue() {
    BranchName branch1 = new BranchName("develop");
    BranchName branch2 = new BranchName("develop");
    BranchName branch3 = new BranchName("hotfix/bug-fix");

    assertEquals(branch1, branch2);
    assertNotEquals(branch1, branch3);
    assertEquals(branch1.hashCode(), branch2.hashCode());
  }
}
