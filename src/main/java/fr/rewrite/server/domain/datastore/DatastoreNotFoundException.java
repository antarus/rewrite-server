package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class DatastoreNotFoundException extends RewriteServerException {

  public DatastoreNotFoundException(RewriteId rewriteId) {
    super(badRequest(DatastoreErrorKey.DATASTORE_NOT_FOUND).addParameter("id", rewriteId.get().toString()));
  }
}
