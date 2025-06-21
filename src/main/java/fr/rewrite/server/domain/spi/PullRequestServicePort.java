package fr.rewrite.server.domain.spi;

import fr.rewrite.server.application.dto.PlatformConfig;
import fr.rewrite.server.application.dto.PullRequestDetails;
import fr.rewrite.server.domain.exception.GitOperationException;

public interface PullRequestServicePort {
  void createPullRequest(PullRequestDetails details, PlatformConfig config) throws GitOperationException;
}
