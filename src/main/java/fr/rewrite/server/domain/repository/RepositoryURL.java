package fr.rewrite.server.domain.repository;

import fr.rewrite.server.domain.exception.RewriteException;
import fr.rewrite.server.shared.error.domain.Assert;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record RepositoryURL(String url) {
  private static final Pattern GITHUB_URL_PATTERN = Pattern.compile(
    "^(https?://|git@)github\\.com(?:\\:|/)(?<owner>[^/]+)/(?<repo>[^/.]+?)(\\.git)?$"
  );
  private static final Pattern GITLAB_URL_PATTERN = Pattern.compile(
    "^(https?://|git@)gitlab\\.com(?:\\:|/)(?<owner>[^/]+)/(?<repo>[^/.]+?)(\\.git)?$"
  );

  public RepositoryURL {
    Assert.field("value", url).notBlank();
    if (!isValidUrl(url)) {
      throw RewriteException.badRequest("Invalid repository URL format: " + url);
    }
  }

  public static RepositoryURL from(String value) {
    return new RepositoryURL(value);
  }
  public String get() {
    return url;
  }
  private boolean isValidUrl(String url) {
    return GITHUB_URL_PATTERN.matcher(url).matches() || GITLAB_URL_PATTERN.matcher(url).matches();
  }

  public String getOwnerName() {
    Matcher githubMatcher = GITHUB_URL_PATTERN.matcher(url);
    if (githubMatcher.matches()) {
      return githubMatcher.group("owner");
    }

    Matcher gitlabMatcher = GITLAB_URL_PATTERN.matcher(url);
    if (gitlabMatcher.matches()) {
      return gitlabMatcher.group("owner");
    }
    return "";
  }

  public String getRepositoryName() {
    Matcher githubMatcher = GITHUB_URL_PATTERN.matcher(url);
    if (githubMatcher.matches()) {
      return githubMatcher.group("repo");
    }

    Matcher gitlabMatcher = GITLAB_URL_PATTERN.matcher(url);
    if (gitlabMatcher.matches()) {
      return gitlabMatcher.group("repo");
    }
    return "";
  }

  public String getPlatform() {
    if (GITHUB_URL_PATTERN.matcher(url).matches()) {
      return "GITHUB";
    }
    if (GITLAB_URL_PATTERN.matcher(url).matches()) {
      return "GITLAB";
    }
    return "UNKNOWN";
  }

  @Override
  public String toString() {
    return url;
  }
}
