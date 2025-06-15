package fr.rewrite.server.application.dto;

public record PlatformConfig(
  String apiBaseUrl,
  String apiToken,
  String repoOwner,
  String repoName,
  String gitlabProjectId,
  String platformType // : "github" ou "gitlab"
) {}
