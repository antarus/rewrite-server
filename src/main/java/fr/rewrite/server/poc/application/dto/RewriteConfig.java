package fr.rewrite.server.poc.application.dto;

import java.util.List;

public record RewriteConfig(
  String repoUrl,
  String recipeName,
  String gitUsername, // <-- NOUVEL ATTRIBUT
  String gitPatForGit, // <-- NOUVEL ATTRIBUT
  String gitPatForApi, // <-- NOUVEL ATTRIBUT
  String platform,
  String baseBranch,
  String mavenExecutablePath,
  boolean pushAndPr,
  List<String> sourceExcludePatterns,
  String commitMessage,
  String prMrTitle,
  String prMrDescription
) {}
