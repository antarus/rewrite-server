package fr.rewrite.server.domain.build;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class BuildClassPathException extends RewriteServerException {

  public BuildClassPathException(DatastoreId datastoreId, Exception cause) {
    super(badRequest(BuildErrorKey.BUILD_OPERATION_ERROR).cause(cause)
            .addParameter("id",datastoreId.uuid().toString()));
  }
}
