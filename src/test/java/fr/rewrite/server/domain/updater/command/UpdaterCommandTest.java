package fr.rewrite.server.domain.updater.command;

import static org.junit.jupiter.api.Assertions.*;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.domain.updater.Recipe;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import org.junit.jupiter.api.Test;

@UnitTest
class UpdaterCommandTest {

  @Test
  void shouldBuild() {
    Recipe recipe = new Recipe("recipe");
    DatastoreId dsId = DatastoreId.from(RepositoryURL.from("https://github.com/test/repo"));
    UpdaterCommand cmd = new UpdaterCommand(dsId, recipe);
    assert (true);
  }

  @Test
  void shouldThrowExceptionWhenRecipeIsNull() {
    DatastoreId dsId = DatastoreId.from(RepositoryURL.from("https://github.com/test/repo"));
    assertThrows(MissingMandatoryValueException.class, () -> new UpdaterCommand(dsId, null));
  }

  @Test
  void shouldThrowExceptionWhenDatastoreIsNull() {
    Recipe recipe = new Recipe("recipe");
    assertThrows(MissingMandatoryValueException.class, () -> new UpdaterCommand(null, recipe));
  }
}
