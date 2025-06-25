package fr.rewrite.server.wire.configuration;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.rewrite.server.domain.datastore.DatastorePort;
import fr.rewrite.server.domain.events.DomainEventHandlerService;
import fr.rewrite.server.domain.repository.RepositoryPort;
import fr.rewrite.server.domain.spi.BuildToolPort;
import fr.rewrite.server.domain.spi.EventBusPort;
import fr.rewrite.server.domain.spi.PullRequestServicePort;
import fr.rewrite.server.domain.spi.RewriteEnginePort;
import fr.rewrite.server.domain.state.RewriteConfig;
import fr.rewrite.server.domain.state.StateRepository;
import fr.rewrite.server.infrastructure.poc.GitHubApiAdapter;
import fr.rewrite.server.infrastructure.poc.GitLabApiAdapter;
import fr.rewrite.server.infrastructure.poc.MavenBuildToolAdapter;
import fr.rewrite.server.infrastructure.poc.OpenRewriteAdapter;
import fr.rewrite.server.infrastructure.secondary.event.EventServiceAdapter;
import fr.rewrite.server.infrastructure.secondary.event.InMemoryEventBusAdapter;
import fr.rewrite.server.infrastructure.secondary.filesystem.JsonFileSystemRepository;
import fr.rewrite.server.infrastructure.secondary.repository.JGitAdapter;
import fr.rewrite.server.poc.application.RewriteOrchestrator;
import java.net.http.HttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
class BeanConfiguration {

  @Bean
  public StateRepository stateRepository(RewriteConfig rewriteConfig) {
    return new JsonFileSystemRepository(rewriteConfig);
  }

  @Bean
  @Primary
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    return builder.createXmlMapper(false).build().findAndRegisterModules().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
  }

  @Bean
  @Profile("!rabbit")
  public EventBusPort eventBusPort() {
    return new InMemoryEventBusAdapter();
  }

  @Bean
  public EventServiceAdapter repositoryCreatedNotificationServiceAdapter(
    EventBusPort eventBusPort,
    DomainEventHandlerService domainEventHandlerService,
    Environment environment
  ) {
    return new EventServiceAdapter(eventBusPort, domainEventHandlerService, environment);
  }

  @Bean
  public RepositoryPort gitRepositoryPort(RewriteConfig rewriteConfig) {
    return new JGitAdapter(rewriteConfig);
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
  public HttpClient httpClient() {
    return HttpClient.newHttpClient();
  }

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
  public RewriteOrchestrator rewriteOrchestrator(
    RepositoryPort gitRepositoryPort,
    BuildToolPort buildToolPort,
    RewriteEnginePort rewriteEnginePort,
    DatastorePort fileSystemPort,
    // NOUVEAU : Utilisez @Qualifier pour spécifier quel bean de type PullRequestServicePort injecter
    @Qualifier("platformServiceSelector") PullRequestServicePort pullRequestServicePort // Renommé pour plus de clarté
  ) {
    return new RewriteOrchestrator(gitRepositoryPort, buildToolPort, rewriteEnginePort, fileSystemPort, pullRequestServicePort);
  }
}
