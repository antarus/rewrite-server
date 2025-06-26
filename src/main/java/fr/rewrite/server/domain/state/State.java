package fr.rewrite.server.domain.state;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.shared.error.domain.Assert;
import java.time.Instant;

public record State(RewriteId rewriteId, StateEnum status, Instant createdAt, Instant updatedAt) {
  public State {
    Assert.notNull("rewriteId", rewriteId);
    Assert.field("createdAt", createdAt).inPast();
    Assert.field("updatedAt", updatedAt).inPast();
  }
  public static State init(RewriteId rewriteId) {
    Instant now = Instant.now();
    return new State(rewriteId, StateEnum.INIT, now, now);
  }

  public State withStatus(StateEnum newStatus) {
    return new State(this.rewriteId, newStatus, this.createdAt, Instant.now());
  }
}
