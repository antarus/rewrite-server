package fr.rewrite.server.domain.repository;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.state.StateEnum;
import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class InvalidStateTransitionException extends RewriteServerException {

  public InvalidStateTransitionException(RewriteId rewriteId, StateEnum from, StateEnum to) {
    super(
      badRequest(RepositoryErrorKey.INVALID_STATE_TRANSITION)
        .addParameter("id", rewriteId.get().toString())
        .addParameter("from", from.toString())
        .addParameter("to", to.toString())
    );
  }
}
