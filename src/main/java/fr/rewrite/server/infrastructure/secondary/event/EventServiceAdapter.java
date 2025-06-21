package fr.rewrite.server.infrastructure.secondary.event;

import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.domain.events.DomainEventHandlerService;
import fr.rewrite.server.domain.spi.EventBusPort;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class EventServiceAdapter {

  public final DomainEventHandlerService domainEventHandlerService;
  public final Environment env;
  private static final Logger log = LoggerFactory.getLogger(EventServiceAdapter.class);

  public EventServiceAdapter(EventBusPort eventBus, DomainEventHandlerService domainEventHandlerService, Environment env) {
    this.domainEventHandlerService = domainEventHandlerService;
    this.env = env;
    if (!Arrays.asList(env.getActiveProfiles()).contains("rabbit")) {
      eventBus.subscribeAll(this::handleDomainEvent);
    }
  }

  private void handleDomainEvent(DomainEvent event) {
    log.trace("EventServiceAdapter: Received DomainEvent {}", event.eventId());
    domainEventHandlerService.handleEvent(event);
  }
}
