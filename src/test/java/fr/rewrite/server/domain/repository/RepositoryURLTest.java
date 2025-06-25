package fr.rewrite.server.domain.repository;

import static org.junit.jupiter.api.Assertions.*;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@UnitTest
public class RepositoryURLTest {

  @Test
  @DisplayName("Should create RepositoryURL with a valid GitHub HTTPS URL")
  public void shouldCreateRepositoryURLWithValidGitHubHttpsUrl() {
    String url = "https://github.com/owner/repo.git";
    RepositoryURL repoUrl = RepositoryURL.from(url);
    assertNotNull(repoUrl);
    assertEquals(url, repoUrl.get());
  }

  @Test
  @DisplayName("Should create RepositoryURL with a valid GitLab SSH URL")
  public void shouldCreateRepositoryURLWithValidGitLabSshUrl() {
    String url = "git@gitlab.com:user/project.git";
    RepositoryURL repoUrl = RepositoryURL.from(url);
    assertNotNull(repoUrl);
    assertEquals(url, repoUrl.get());
  }

  @Test
  @DisplayName("Should create RepositoryURL with a valid GitHub HTTPS URL without .git suffix")
  public void shouldCreateRepositoryURLWithValidGitHubHttpsUrlNoGit() {
    String url = "https://github.com/octocat/Spoon-Knife";
    RepositoryURL repoUrl = RepositoryURL.from(url);
    assertNotNull(repoUrl);
    assertEquals(url, repoUrl.get());
  }

  @Test
  @DisplayName("Should throw MissingMandatoryValueException for null or empty URLs")
  public void shouldThrowExceptionForNullUrls() {
    assertThrows(MissingMandatoryValueException.class, () -> new RepositoryURL(null));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { " ", "\t", "\n" })
  @DisplayName("Should throw MissingMandatoryValueException for null or empty URLs")
  void shouldThrowExceptionForNullOrEmptyUrls(String invalidUrl) {
    assertThrows(MissingMandatoryValueException.class, () -> new RepositoryURL(invalidUrl));
  }

  @ParameterizedTest
  @ValueSource(
    strings = { "http://invalid.url/path", "ftp://another.protocol.com/repo", "just_a_string", "https://not-github-gitlab.com/owner/repo" }
  )
  @DisplayName("Should throw RewriteException for invalid format URLs")
  void shouldThrowExceptionForInvalidFormatUrls(String invalidUrl) {
    assertThrows(RepositoryInvalidUrlException.class, () -> new RepositoryURL(invalidUrl));
  }

  @ParameterizedTest
  @CsvSource(
    {
      "https://github.com/spring-projects/spring-boot.git, spring-projects, spring-boot, GITHUB",
      "git@github.com:octocat/Spoon-Knife.git, octocat, Spoon-Knife, GITHUB",
      "git@gitlab.com:user/my-app, user, my-app, GITLAB",
      "https://gitlab.com/gitlab-org/gitlab-foss.git, gitlab-org, gitlab-foss, GITLAB",
      "https://github.com/someuser/another-repo, someuser, another-repo, GITHUB",
    }
  )
  @DisplayName("Should correctly extract owner name, repository name, and platform")
  void shouldExtractOwnerRepoAndPlatformCorrectly(String url, String expectedOwner, String expectedRepo, String expectedPlatform) {
    RepositoryURL repoUrl = RepositoryURL.from(url);
    assertEquals(expectedOwner, repoUrl.getOwnerName());
    assertEquals(expectedRepo, repoUrl.getRepositoryName());
    assertEquals(expectedPlatform, repoUrl.getPlatform());
  }

  @Test
  @DisplayName("Should test RepositoryURL equality based on value")
  public void shouldTestEqualityBasedOnValue() {
    RepositoryURL url1 = new RepositoryURL("https://github.com/owner/repo.git");
    RepositoryURL url2 = new RepositoryURL("https://github.com/owner/repo.git");
    RepositoryURL url3 = new RepositoryURL("https://gitlab.com/beyondxscratch/hexagonal-architecture-java-springboot.git");

    assertEquals(url1, url2);
    assertNotEquals(url1, url3);
    assertEquals(url1.hashCode(), url2.hashCode());
  }

  @Test
  @DisplayName("Should test RepositoryURL compatible with bitbucket")
  public void shouldOkWithBitbucket() {
    RepositoryURL url1 = new RepositoryURL("https://bitbucket.org/atlassian/pipelines-examples-rust.git");
    assertNotNull(url1);
    assertEquals("atlassian", url1.getOwnerName());
    assertEquals("GIT", url1.getPlatform());
    assertEquals("pipelines-examples-rust", url1.getRepositoryName());
  }
}
