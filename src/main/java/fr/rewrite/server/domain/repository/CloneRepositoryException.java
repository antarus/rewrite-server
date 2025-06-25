package fr.rewrite.server.domain.repository;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class CloneRepositoryException extends RewriteServerException {

  public CloneRepositoryException(RepositoryURL repositoryURL, Exception cause) {
    super(
      badRequest(RepositoryErrorKey.REPOSITORY_CLONE_ERROR)
        .cause(cause)
        .addParameter("id", RewriteId.from(repositoryURL).get().toString())
        .addParameter("repository.url", repositoryURL.toString())
    );
  }
}
