package fr.rewrite.server.domain.updater;

import static org.junit.jupiter.api.Assertions.*;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import org.junit.jupiter.api.Test;

@UnitTest
class RecipeTest {

  @Test
  void shouldBuild() {
    Recipe recipe = new Recipe("test");
    assertTrue(true);
  }

  @Test
  void shouldThrowExceptionBuild() {
    assertThrows(MissingMandatoryValueException.class, () -> new Recipe(null));
  }

  @Test
  void shouldThrowExceptionWhenEmptyBuild() {
    assertThrows(MissingMandatoryValueException.class, () -> new Recipe(""));
  }
}
