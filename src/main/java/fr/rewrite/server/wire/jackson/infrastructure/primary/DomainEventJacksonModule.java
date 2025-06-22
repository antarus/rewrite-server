package fr.rewrite.server.wire.jackson.infrastructure.primary;

import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.rewrite.server.domain.events.DomainEvent;

class DomainEventJacksonModule extends SimpleModule {

  public DomainEventJacksonModule() {
    super("DomainEventModule");
    setMixInAnnotation(DomainEvent.class, DomainEventMixIn.class);
  }
}
