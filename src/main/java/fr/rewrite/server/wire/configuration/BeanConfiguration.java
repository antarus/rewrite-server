package fr.rewrite.server.wire.configuration;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.rewrite.server.domain.EventPublisher;
import fr.rewrite.server.domain.RewriteConfig;
import fr.rewrite.server.domain.build.BuildPort;
import fr.rewrite.server.domain.datastore.DatastorePort;
import fr.rewrite.server.domain.datastore.event.DatastoreEvent;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentStateRepository;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentStateUpdater;
import fr.rewrite.server.domain.log.LogPublisher;
import fr.rewrite.server.domain.repository.RepositoryPort;
import fr.rewrite.server.infrastructure.secondary.build.BuildAdapter;
import fr.rewrite.server.infrastructure.secondary.build.MavenBuildTool;
import fr.rewrite.server.infrastructure.secondary.build.Tool;
import fr.rewrite.server.infrastructure.secondary.repository.JGitAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
class BeanConfiguration {

  @Bean
  @Primary
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    return builder.createXmlMapper(false).build().findAndRegisterModules().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
  }

  @Bean
  public RepositoryPort gitRepositoryPort(RewriteConfig rewriteConfig, DatastorePort datastorePort, LogPublisher logPublisher) {
    return new JGitAdapter(rewriteConfig, datastorePort, logPublisher);
  }

  @Bean("mavenTool")
  public Tool mavenTool(RewriteConfig rewriteConfig, LogPublisher logPublisher) {
    return new MavenBuildTool(rewriteConfig, logPublisher);
  }

  @Bean
  public BuildPort gitBuildPort(RewriteConfig rewriteConfig, @Qualifier("mavenTool") Tool mavenTool) {
    return new BuildAdapter(rewriteConfig, mavenTool);
  }

  @Bean
  public EventPublisher<DatastoreEvent> eventPublisherDatsyore(DatastoreCurrentStateRepository currentStateRepository) {
    return new EventPublisher<DatastoreEvent>().register(new DatastoreCurrentStateUpdater(currentStateRepository));
  }
}
