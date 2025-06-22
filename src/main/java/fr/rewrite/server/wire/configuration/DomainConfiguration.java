package fr.rewrite.server.wire.configuration;

import fr.rewrite.server.domain.datastore.DatastoreWorker;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.ddd.Stub;
import fr.rewrite.server.domain.events.DomainEventHandlerService;
import fr.rewrite.server.domain.repository.RepositoryWorker;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
  basePackageClasses = { DatastoreWorker.class, RepositoryWorker.class, DomainEventHandlerService.class },
  includeFilters = { @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = { DomainService.class, Stub.class }) }
)
//        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {Stub.class})})
class DomainConfiguration {}
