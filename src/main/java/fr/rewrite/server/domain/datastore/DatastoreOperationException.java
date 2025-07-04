package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.shared.error.domain.ErrorStatus;
import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class DatastoreOperationException extends RewriteServerException {

  public DatastoreOperationException(String message) {
    super(internalServerError(DatastoreErrorKey.DATASTORE_OPERATION_ERROR).message(message));
  }

  public DatastoreOperationException(String message, Throwable cause) {
    super(
      internalServerError(DatastoreErrorKey.DATASTORE_OPERATION_ERROR)
        .message(message)
        .cause(cause)
        .status(ErrorStatus.INTERNAL_SERVER_ERROR)
    );
  }
}
