package fr.rewrite.server.wire.configuration;

import static fr.rewrite.server.domain.state.RewriteConfig.RewriteConfigBuilder.aRewriteConfig;

import fr.rewrite.server.domain.state.RewriteConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RewriteConfiguration {

  @Value("${rewrite.server.work}")
  private String workDirectory;

  @Value("${rewrite.server.config}")
  private String configDirectory;

  @Bean
  public RewriteConfig rewriteConfig() {
    return aRewriteConfig().configDirectory(configDirectory).workDirectory(workDirectory).build();
  }
}
