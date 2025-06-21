package fr.rewrite.server.infrastructure.secondary.api;

import fr.rewrite.server.application.dto.PlatformConfig;
import fr.rewrite.server.application.dto.PullRequestDetails;
import fr.rewrite.server.domain.exception.GitOperationException;
import fr.rewrite.server.domain.spi.PullRequestServicePort;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GitLabApiAdapter implements PullRequestServicePort {

  private final HttpClient httpClient;

  public GitLabApiAdapter(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public void createPullRequest(PullRequestDetails details, PlatformConfig config) throws GitOperationException {
    String endpointUrl = String.format("%s/projects/%s/merge_requests", config.apiBaseUrl(), config.gitlabProjectId());
    String requestBody = String.format(
      "{\"source_branch\":\"%s\",\"target_branch\":\"%s\",\"title\":\"%s\",\"description\":\"%s\",\"remove_source_branch\":%b}",
      details.headBranch(),
      details.baseBranch(),
      details.title(),
      details.description() + "Commit: " + details.commitId(),
      true // Supprimer la branche source après merge
    );

    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(endpointUrl))
      .header("Private-Token", config.apiToken())
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(requestBody))
      .build();
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 200 && response.statusCode() < 300) {
        System.out.println("GitLab Merge Request created successfully!");
        System.out.println("Response: " + response.body());
      } else {
        System.err.println("Failed to create GitLab Merge Request. Status: " + response.statusCode());
        System.err.println("Response body: " + response.body());
        throw new GitOperationException("Failed to create GitLab MR: " + response.body());
      }
    } catch (IOException | InterruptedException e) {
      throw new GitOperationException("Failed to send GitLab MR: " + e.getMessage());
    }
  }
}
