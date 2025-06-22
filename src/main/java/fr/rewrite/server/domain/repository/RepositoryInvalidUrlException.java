package fr.rewrite.server.domain.repository;

import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class RepositoryInvalidUrlException extends RewriteServerException {

  public RepositoryInvalidUrlException(String repositoryURL) {
    super(
      badRequest(RepositoryErrorKey.REPOSITORY_INVALID_URI)
        .message(buildMessage(repositoryURL))
        .addParameter("repository.url", repositoryURL)
    );
  }

  private static String buildMessage(String repositoryURL) {
    return new StringBuilder().append("Url '").append(repositoryURL).append("' is not valid").toString();
  }
}
