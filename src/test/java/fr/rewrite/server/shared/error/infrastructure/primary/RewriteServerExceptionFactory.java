package fr.rewrite.server.shared.error.infrastructure.primary;

import fr.rewrite.server.shared.error.domain.RewriteServerException;

public final class RewriteServerExceptionFactory {

  private RewriteServerExceptionFactory() {}

  public static RewriteServerException buildEmptyException() {
    return RewriteServerException.builder(null).build();
  }
}
