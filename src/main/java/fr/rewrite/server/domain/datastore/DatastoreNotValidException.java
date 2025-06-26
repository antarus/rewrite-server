package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class DatastoreNotValidException extends RewriteServerException {

  public DatastoreNotValidException(RewriteId rewriteId) {
    super(badRequest(DatastoreErrorKey.DATASTORE_NOT_VALID).addParameter("id", rewriteId.get().toString()));
  }
}
