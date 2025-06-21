package fr.rewrite.server.infrastructure.poc;

import fr.rewrite.server.domain.exception.GitOperationException;
import fr.rewrite.server.domain.spi.PullRequestServicePort;
import fr.rewrite.server.poc.application.dto.PlatformConfig;
import fr.rewrite.server.poc.application.dto.PullRequestDetails;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GitHubApiAdapter implements PullRequestServicePort {

  private final HttpClient httpClient;

  public GitHubApiAdapter(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  //  @Override
  public void createPullRequest(PullRequestDetails details, PlatformConfig config) throws GitOperationException {
    String endpointUrl = String.format("%s/repos/%s/%s/pulls", config.apiBaseUrl(), config.repoOwner(), config.repoName());
    String requestBody = String.format(
      "{\"head\":\"%s\",\"base\":\"%s\",\"title\":\"%s\",\"body\":\"%s\"}",
      details.headBranch(),
      details.baseBranch(),
      details.title(),
      details.description() + "Commit: " + details.commitId()
    );

    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(endpointUrl))
      .header("Authorization", "token " + config.apiToken())
      .header("Accept", "application/vnd.github.v3+json")
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(requestBody))
      .build();
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 200 && response.statusCode() < 300) {
        System.out.println("GitHub Pull Request created successfully!");
        System.out.println("Response: " + response.body());
      } else {
        System.err.println("Failed to create GitHub Pull Request. Status: " + response.statusCode());
        System.err.println("Response body: " + response.body());
        throw new GitOperationException("Failed to create GitHub PR: " + response.body());
      }
    } catch (IOException | InterruptedException e) {
      throw new GitOperationException("Failed to send GitHub PR: " + e.getMessage());
    }
  }
}
