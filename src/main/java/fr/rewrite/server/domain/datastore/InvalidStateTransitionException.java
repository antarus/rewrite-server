package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.StatusEnum;
import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class InvalidStateTransitionException extends RewriteServerException {

  public InvalidStateTransitionException(DatastoreId datastoreId, StatusEnum from, StatusEnum to) {
    super(
      badRequest(DatastoreErrorKey.INVALID_STATE_TRANSITION)
        .message(String.format("Invalid state transition from %s to %s", from, to))
        .addParameter("id", datastoreId.get().toString())
        .addParameter("from", from.toString())
        .addParameter("to", to.toString())
    );
  }
}
