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
class RepositoryNameTest {

  @Test
  @DisplayName("Should create RepositoryName with a valid name")
  void shouldCreateRepositoryNameWithValidName() {
    String name = "my-awesome-repo";
    RepositoryName repoName = new RepositoryName(name);
    assertNotNull(repoName);
    assertEquals(name, repoName.get());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { " ", "\t", "\n" })
  @DisplayName("Should throw IllegalArgumentException for null or empty names")
  void shouldThrowExceptionForNullOrEmptyNames(String invalidName) {
    assertThrows(MissingMandatoryValueException.class, () -> new RepositoryName(invalidName));
  }

  @Test
  @DisplayName("Should test RepositoryName equality based on value")
  void shouldTestEqualityBasedOnValue() {
    RepositoryName name1 = new RepositoryName("project-alpha");
    RepositoryName name2 = new RepositoryName("project-alpha");
    RepositoryName name3 = new RepositoryName("project-beta");

    assertEquals(name1, name2);
    assertNotEquals(name1, name3);
    assertEquals(name1.hashCode(), name2.hashCode());
  }
}
