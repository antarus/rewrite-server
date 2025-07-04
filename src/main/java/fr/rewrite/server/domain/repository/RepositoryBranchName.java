package fr.rewrite.server.domain.repository;

import java.util.regex.Pattern;

public record RepositoryBranchName(String name) {
  private static final Pattern STARTS_WITH_SLASH = Pattern.compile("^/");
  private static final Pattern ENDS_WITH_SLASH = Pattern.compile("/$");
  private static final Pattern ENDS_WITH_DOT = Pattern.compile("\\.$");
  private static final Pattern CONTAINS_DOUBLE_SLASH = Pattern.compile("//");
  private static final Pattern CONTAINS_DOUBLE_DOT = Pattern.compile("\\.\\.");
  private static final Pattern ENDS_WITH_LOCK = Pattern.compile("\\.lock$");
  private static final Pattern IS_EXACTLY_HEAD = Pattern.compile("^HEAD$");
  private static final Pattern CONTAINS_GIT_PATH = Pattern.compile("\\.git/");
  private static final Pattern CONTAINS_AT_BRACE = Pattern.compile("@\\{");
  private static final Pattern CONTAINS_AT_DOT = Pattern.compile("@\\.");

  private static final Pattern CONTAINS_FORBIDDEN_CHARACTERS = Pattern.compile(
    "[\\s~^?*()\\[\\]{}@:;\\\\#!$&]" // Matches any of: whitespace, ~, ^, ?, *, (, ), [, ], {, }, @, :, ;, \, #, !, $, &
  );

  public RepositoryBranchName {
    if (!isValidBranchName(name)) {
      throw new IllegalArgumentException("The Git branch name '" + name + "' is invalid.");
    }
  }

  private boolean isValidBranchName(String branchName) {
    if (branchName == null || branchName.isEmpty()) {
      return false;
    }
    if (STARTS_WITH_SLASH.matcher(branchName).find()) {
      return false;
    }
    if (ENDS_WITH_SLASH.matcher(branchName).find()) {
      return false;
    }
    if (ENDS_WITH_DOT.matcher(branchName).find()) {
      return false;
    }
    if (CONTAINS_DOUBLE_SLASH.matcher(branchName).find()) {
      return false;
    }
    if (CONTAINS_DOUBLE_DOT.matcher(branchName).find()) {
      return false;
    }
    if (ENDS_WITH_LOCK.matcher(branchName).find()) {
      return false;
    }
    if (IS_EXACTLY_HEAD.matcher(branchName).matches()) {
      return false;
    }
    if (CONTAINS_GIT_PATH.matcher(branchName).find()) {
      return false;
    }
    if (CONTAINS_AT_BRACE.matcher(branchName).find()) {
      return false;
    }
    if (CONTAINS_AT_DOT.matcher(branchName).find()) {
      return false;
    }

    if (CONTAINS_FORBIDDEN_CHARACTERS.matcher(branchName).find()) {
      return false;
    }

    Pattern CONTAINS_NON_ALLOWED_CHARACTERS = Pattern.compile("[^a-zA-Z0-9_/.-]");
    if (CONTAINS_NON_ALLOWED_CHARACTERS.matcher(branchName).find()) {
      return false;
    }
    return true;
  }

  public static RepositoryBranchName from(String branchName) {
    return new RepositoryBranchName(branchName);
  }
}
