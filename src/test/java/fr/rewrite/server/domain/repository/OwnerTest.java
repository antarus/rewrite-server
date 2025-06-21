package fr.rewrite.server.domain.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class OwnerTest {

  @Test
  @DisplayName("Should create Owner with a valid name")
  void shouldCreateOwnerWithValidName() {
    String name = "organization-name";
    Owner owner = new Owner(name);
    assertNotNull(owner);
    assertEquals(name, owner.name());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { " ", "\t", "\n" })
  @DisplayName("Should throw IllegalArgumentException for null or empty Owner names")
  void shouldThrowExceptionForNullOrEmptyOwnerNames(String invalidName) {
    assertThrows(MissingMandatoryValueException.class, () -> new Owner(invalidName));
  }

  @Test
  @DisplayName("Should test Owner equality based on value")
  void shouldTestEqualityBasedOnValue() {
    Owner owner1 = new Owner("user123");
    Owner owner2 = new Owner("user123");
    Owner owner3 = new Owner("another-user");

    assertEquals(owner1, owner2);
    assertNotEquals(owner1, owner3);
    assertEquals(owner1.hashCode(), owner2.hashCode());
  }
}
