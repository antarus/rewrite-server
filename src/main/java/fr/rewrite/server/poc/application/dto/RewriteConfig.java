package fr.rewrite.server.poc.application.dto;

import java.util.List;

public record RewriteConfig(
  String repoUrl,
  String recipeName,
  String gitUsername,
  String gitPatForGit,
  String gitPatForApi,
  String platform,
  String baseBranch,
  String mavenExecutablePath,
  boolean pushAndPr,
  List<String> sourceExcludePatterns,
  String commitMessage,
  String prMrTitle,
  String prMrDescription
) {}
