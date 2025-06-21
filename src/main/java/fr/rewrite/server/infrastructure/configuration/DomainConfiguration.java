package fr.rewrite.server.infrastructure.configuration;

import fr.rewrite.server.domain.RepoRewriter;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.ddd.Stub;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
  basePackageClasses = { RepoRewriter.class },
  includeFilters = { @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = { DomainService.class, Stub.class }) }
)
//        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {StarShipInventoryStub.class})})
public class DomainConfiguration {}
