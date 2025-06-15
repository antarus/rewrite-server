package fr.rewrite.server;

import fr.rewrite.server.domain.*;
import fr.rewrite.server.infrastructure.secondary.api.GitHubApiAdapter;
import fr.rewrite.server.infrastructure.secondary.api.GitLabApiAdapter;
import fr.rewrite.server.infrastructure.secondary.buildtool.MavenBuildToolAdapter;
import fr.rewrite.server.infrastructure.secondary.filesystem.NioFileSystemAdapter;
import fr.rewrite.server.infrastructure.secondary.git.JGitAdapter;
import fr.rewrite.server.infrastructure.secondary.rewrite.OpenRewriteAdapter;
import java.net.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@SpringBootApplication
@Configuration
public class RewriteServerApp {

  private static final Logger log = LoggerFactory.getLogger(RewriteServerApp.class);

  public static void main(String[] args) {
    Environment env = SpringApplication.run(RewriteServerApp.class, args).getEnvironment();

    if (log.isInfoEnabled()) {
      log.info(ApplicationStartupTraces.of(env));
    }
  }

  @Bean
  public GitRepositoryPort gitRepositoryPort() {
    return new JGitAdapter();
  }

  @Bean
  public BuildToolPort buildToolPort() {
    return new MavenBuildToolAdapter();
  }

  @Bean
  public RewriteEnginePort rewriteEnginePort() {
    return new OpenRewriteAdapter();
  }

  @Bean
  public FileSystemPort fileSystemPort() {
    return new NioFileSystemAdapter();
  }

  @Bean
  public HttpClient httpClient() {
    return HttpClient.newHttpClient();
  }

  // Ces beans sont nécessaires pour être injectés dans PlatformServiceSelector
  // Nous leur donnons des noms explicites avec @Bean("nomDuBean")
  @Bean("gitHubApiAdapter")
  public GitHubApiAdapter gitHubApiAdapter(HttpClient httpClient) {
    return new GitHubApiAdapter(httpClient);
  }

  @Bean("gitLabApiAdapter")
  public GitLabApiAdapter gitLabApiAdapter(HttpClient httpClient) {
    return new GitLabApiAdapter(httpClient);
  }

  // Le PlatformServiceSelector est une classe @Service, Spring crée automatiquement un bean pour elle.
  // Son nom de bean par défaut sera "platformServiceSelector" (nom de classe en camelCase).

  @Bean
  public fr.rewrite.server.application.RewriteOrchestrator rewriteOrchestrator(
    GitRepositoryPort gitRepositoryPort,
    BuildToolPort buildToolPort,
    RewriteEnginePort rewriteEnginePort,
    FileSystemPort fileSystemPort,
    // NOUVEAU : Utilisez @Qualifier pour spécifier quel bean de type PullRequestServicePort injecter
    @Qualifier("platformServiceSelector") PullRequestServicePort pullRequestServicePort // Renommé pour plus de clarté
  ) {
    return new fr.rewrite.server.application.RewriteOrchestrator(
      gitRepositoryPort,
      buildToolPort,
      rewriteEnginePort,
      fileSystemPort,
      pullRequestServicePort
    );
  }
}
