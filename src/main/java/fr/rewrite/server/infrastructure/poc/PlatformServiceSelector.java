package fr.rewrite.server.infrastructure.secondary;

import fr.rewrite.server.application.dto.PlatformConfig;
import fr.rewrite.server.application.dto.PullRequestDetails;
import fr.rewrite.server.domain.exception.GitOperationException;
import fr.rewrite.server.domain.spi.PullRequestServicePort;
import fr.rewrite.server.infrastructure.poc.GitHubApiAdapter;
import fr.rewrite.server.infrastructure.poc.GitLabApiAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PlatformServiceSelector implements PullRequestServicePort {

  private final GitHubApiAdapter githubAdapter;
  private final GitLabApiAdapter gitlabAdapter;

  public PlatformServiceSelector(
    @Qualifier("gitHubApiAdapter") GitHubApiAdapter githubAdapter,
    @Qualifier("gitLabApiAdapter") GitLabApiAdapter gitlabAdapter
  ) {
    this.githubAdapter = githubAdapter;
    this.gitlabAdapter = gitlabAdapter;
  }

  @Override
  public void createPullRequest(PullRequestDetails details, PlatformConfig config) throws GitOperationException {
    // MAINTENANT config.platform() existe bien
    if ("github".equalsIgnoreCase(config.platformType())) {
      githubAdapter.createPullRequest(details, config);
    } else if ("gitlab".equalsIgnoreCase(config.platformType())) {
      gitlabAdapter.createPullRequest(details, config);
    } else {
      throw new IllegalArgumentException("Unsupported platform specified in PlatformConfig: " + config.platformType());
    }
  }
}
