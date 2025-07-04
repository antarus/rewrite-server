package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class DatastoreNotFoundException extends RewriteServerException {

  public DatastoreNotFoundException(DatastoreId datastoreId) {
    super(badRequest(DatastoreErrorKey.DATASTORE_NOT_FOUND).addParameter("id", datastoreId.get().toString()));
  }
}
