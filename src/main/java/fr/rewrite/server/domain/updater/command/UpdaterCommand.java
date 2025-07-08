package fr.rewrite.server.domain.updater.command;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.updater.Recipe;
import fr.rewrite.server.shared.error.domain.Assert;

public record UpdaterCommand(DatastoreId datastoreId, Recipe recipe) {
  public UpdaterCommand {
    Assert.notNull("recipe", recipe);
    Assert.notNull("datastoreId", datastoreId);
  }
}
