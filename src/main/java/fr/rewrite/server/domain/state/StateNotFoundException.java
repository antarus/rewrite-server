package fr.rewrite.server.domain.state;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class StateNotFoundException extends RewriteServerException {

  public StateNotFoundException(RewriteId rewriteId) {
    super(badRequest(StateErrorKey.STATE_NOT_FOUND).addParameter("id", rewriteId.get().toString()));
  }
}
