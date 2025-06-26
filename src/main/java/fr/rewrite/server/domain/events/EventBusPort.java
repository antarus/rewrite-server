package fr.rewrite.server.domain.events;

import java.util.function.Consumer;

public interface EventBusPort {
  void publish(DomainEvent event);

  <T extends DomainEvent> void subscribe(Class<T> eventType, Consumer<T> handler);
  void subscribeAll(Consumer<DomainEvent> handler);
}
