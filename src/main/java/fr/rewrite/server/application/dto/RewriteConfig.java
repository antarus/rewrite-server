package fr.rewrite.server.application.dto;

import java.nio.file.Path;
import java.util.List;
import org.openrewrite.ExecutionContext;

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
