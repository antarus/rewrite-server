package fr.rewrite.server.wire.configuration;

import static fr.rewrite.server.domain.RewriteConfig.RewriteConfigBuilder.aRewriteConfig;

import fr.rewrite.server.domain.RewriteConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RewriteConfiguration {

  @Value("${rewrite.server.work-directory}")
  private String workDirectory;

  @Value("${rewrite.server.config-directory}")
  private String configDirectory;

  @Value("${rewrite.server.mvn-path}")
  private String mvnPath;

  @Value("${rewrite.server.datastore.cache-directory}")
  private String datastoreCacheDirectory;

  @Value("${rewrite.server.datastore.repository-directory}")
  private String datastoreRepositoryDirectory;

  @Bean
  public RewriteConfig rewriteConfig() {
    return aRewriteConfig()
      .configDirectory(configDirectory)
      .workDirectory(workDirectory)
      .mvnPath(mvnPath)
      .dsCache(datastoreCacheDirectory)
      .dsRepsository(datastoreRepositoryDirectory)
      .build();
  }
}
