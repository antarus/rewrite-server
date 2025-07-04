package fr.rewrite.server.domain;

import org.jmolecules.event.annotation.DomainEventHandler;

public interface EventHandler<T> {
  @DomainEventHandler
  void handle(T event);
}
