package fr.rewrite.server.domain;

import fr.rewrite.server.shared.error.domain.Assert;

import java.time.Instant;

public record State(RewriteId rewriteId,StateEnum status, Instant createdAt, Instant updatedAt) {
  public State{
    Assert.notNull("rewriteId",rewriteId);
    Assert.field("createdAt",createdAt).inPast();
    Assert.field("updatedAt",updatedAt).inPast();
  }
  public static State init(RewriteId rewriteId) {
    return new State(rewriteId,StateEnum.INIT, Instant.now(), Instant.now());
  }
}
