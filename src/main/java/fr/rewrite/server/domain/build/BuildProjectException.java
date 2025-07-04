package fr.rewrite.server.domain.build;

import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class BuildProjectException extends RewriteServerException {

  public BuildProjectException(DatastoreId datastoreId, Exception cause) {
    super(badRequest(BuildErrorKey.BUILD_OPERATION_ERROR).cause(cause)
            .addParameter("id",datastoreId.uuid().toString()));
  }
  @Deprecated(forRemoval = true)
  public BuildProjectException(Datastore datastore, Exception cause) {
    super(badRequest(BuildErrorKey.BUILD_OPERATION_ERROR).cause(cause)
            .addParameter("id",datastore.datastoreId().uuid().toString()));
  }
}
