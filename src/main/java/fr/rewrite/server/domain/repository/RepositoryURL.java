package fr.rewrite.server.domain.repository;

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

  private static final Pattern GIT_URL_PATTERN = Pattern.compile(
    "(?<host>(git@|https://)([\\w\\.@]+)(/|:))(?<owner>[\\w,\\-,\\_]+)/(?<repo>[\\w,\\-,\\_]+)(.git){0,1}((/){0,1})"
  );
  public static final String REPO = "repo";
  public static final String OWNER = "owner";

  public RepositoryURL {
    Assert.field("url", url).notBlank();
    if (!isValidUrl(url)) {
      throw new RepositoryInvalidUrlException(url);
    }
  }

  public static RepositoryURL from(String url) {
    return new RepositoryURL(url);
  }
  public String get() {
    return url;
  }
  private boolean isValidUrl(String url) {
    return GITHUB_URL_PATTERN.matcher(url).matches() || GITLAB_URL_PATTERN.matcher(url).matches() || GIT_URL_PATTERN.matcher(url).matches();
  }

  public String getOwnerName() {
    Matcher githubMatcher = GITHUB_URL_PATTERN.matcher(url);
    if (githubMatcher.matches()) {
      return githubMatcher.group(OWNER);
    }

    Matcher gitlabMatcher = GITLAB_URL_PATTERN.matcher(url);
    if (gitlabMatcher.matches()) {
      return gitlabMatcher.group(OWNER);
    }

    Matcher gitMatcher = GIT_URL_PATTERN.matcher(url);
    if (gitMatcher.matches()) {
      return gitMatcher.group(OWNER);
    }
    return "";
  }

  public String getRepositoryName() {
    Matcher githubMatcher = GITHUB_URL_PATTERN.matcher(url);
    if (githubMatcher.matches()) {
      return githubMatcher.group(REPO);
    }

    Matcher gitlabMatcher = GITLAB_URL_PATTERN.matcher(url);
    if (gitlabMatcher.matches()) {
      return gitlabMatcher.group(REPO);
    }
    Matcher gitMatcher = GIT_URL_PATTERN.matcher(url);
    if (gitMatcher.matches()) {
      return gitMatcher.group(REPO);
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
    if (GIT_URL_PATTERN.matcher(url).matches()) {
      return "GIT";
    }
    return "UNKNOWN";
  }

  @Override
  public String toString() {
    return url;
  }
}
