package fr.rewrite.server.infrastructure.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.rewrite.server.domain.RewriteConfig;
import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.domain.events.DomainEventHandlerService;
import fr.rewrite.server.domain.spi.*;
import fr.rewrite.server.infrastructure.poc.*;
import fr.rewrite.server.infrastructure.secondary.event.DomainEventMixIn;
import fr.rewrite.server.infrastructure.secondary.event.EventServiceAdapter;
import fr.rewrite.server.infrastructure.secondary.event.InMemoryEventBusAdapter;
import fr.rewrite.server.infrastructure.secondary.filesystem.JsonFileSystemRepository;
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
public class BeanConfiguration {

  @Bean
  public DataRepository dataRepository(RewriteConfig rewriteConfig) {
    return new JsonFileSystemRepository(rewriteConfig);
  }

  //  @Bean
  //  public JobScheduler initJobRunr(DataSource dataSource, JobActivator jobActivator) {
  //    return JobRunr.configure()
  //      .useJobActivator(jobActivator)
  //      .useStorageProvider(SqlStorageProviderFactory.using(dataSource))
  //      .useBackgroundJobServer()
  ////      .useDashboard()
  //      .initialize()
  //      .getJobScheduler();
  //  }

  @Bean
  @Primary
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    return builder.createXmlMapper(false).build().findAndRegisterModules().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
  }

  @Bean("objectMapperEvent")
  public ObjectMapper objectMapperEvent(Jackson2ObjectMapperBuilder builder) {
    ObjectMapper mapper = builder
      .createXmlMapper(false)
      .build()
      .findAndRegisterModules()
      .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    mapper.registerModule(new JavaTimeModule());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().allowIfBaseType("fr.rewrite.server.domain.events").build();

    mapper.addMixIn(DomainEvent.class, DomainEventMixIn.class);

    mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);

    return mapper;
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
    GitRepositoryPort gitRepositoryPort,
    BuildToolPort buildToolPort,
    RewriteEnginePort rewriteEnginePort,
    DatastorePort fileSystemPort,
    // NOUVEAU : Utilisez @Qualifier pour spécifier quel bean de type PullRequestServicePort injecter
    @Qualifier("platformServiceSelector") PullRequestServicePort pullRequestServicePort // Renommé pour plus de clarté
  ) {
    return new RewriteOrchestrator(gitRepositoryPort, buildToolPort, rewriteEnginePort, fileSystemPort, pullRequestServicePort);
  }
}
