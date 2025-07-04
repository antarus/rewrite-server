package fr.rewrite.server.wire.configuration;

import fr.rewrite.server.domain.RewriteConfig;
import fr.rewrite.server.domain.build.BuildDomainService;
import fr.rewrite.server.domain.datastore.DatastoreDomainService;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.ddd.Stub;
import fr.rewrite.server.domain.log.CleanSensitiveLog;
import fr.rewrite.server.domain.log.LogPublisher;
import fr.rewrite.server.domain.repository.RepositoryDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
  basePackageClasses = { DatastoreDomainService.class, RepositoryDomainService.class, BuildDomainService.class, LogPublisher.class },
  includeFilters = { @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = { DomainService.class, Stub.class }) }
)
class DomainConfiguration {

  @Bean
  public CleanSensitiveLog cleanSensitiveLog(RewriteConfig config) {
    return new CleanSensitiveLog(config);
  }
}
