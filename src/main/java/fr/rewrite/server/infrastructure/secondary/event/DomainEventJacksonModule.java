package fr.rewrite.server.infrastructure.secondary.event;

import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.rewrite.server.domain.events.DomainEvent;

public class DomainEventJacksonModule extends SimpleModule {

  public DomainEventJacksonModule() {
    super("DomainEventModule");
    setMixInAnnotation(DomainEvent.class, DomainEventMixIn.class);
  }
}
