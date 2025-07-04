package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class DatastoreNotValidException extends RewriteServerException {

  public DatastoreNotValidException(DatastoreId datastoreId) {
    super(badRequest(DatastoreErrorKey.DATASTORE_NOT_VALID).addParameter("id", datastoreId.get().toString()));
  }
}
