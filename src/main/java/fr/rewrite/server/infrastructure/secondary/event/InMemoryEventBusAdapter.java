package fr.rewrite.server.infrastructure.secondary.event;

import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.domain.events.EventBusPort;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryEventBusAdapter implements EventBusPort {

  private static final Logger log = LoggerFactory.getLogger(InMemoryEventBusAdapter.class);
  private final Map<Class<? extends DomainEvent>, List<Consumer<? extends DomainEvent>>> subscribers;
  private final List<Consumer<DomainEvent>> globalSubscribers;

  public InMemoryEventBusAdapter() {
    this.subscribers = new HashMap<>();
    this.globalSubscribers = new ArrayList<>();
  }

  @Override
  public void publish(DomainEvent event) {
    Class<? extends DomainEvent> eventType = event.getClass();
    log.debug("Publishing event: {} - {} ", eventType.getSimpleName(), event.eventId());

    List<Consumer<? extends DomainEvent>> handlers = subscribers.get(eventType);

    if (handlers != null) {
      for (Consumer<? extends DomainEvent> handler : new ArrayList<>(handlers)) {
        try {
          @SuppressWarnings("unchecked")
          Consumer<DomainEvent> typedHandler = (Consumer<DomainEvent>) handler;
          typedHandler.accept(event);
        } catch (Exception e) {
          log.error("Error processing event {} with handler: {}", event.eventId(), e.getMessage());
          //TODO
        }
      }
    } else {
      log.debug("No handler found for {} - {} ", event.getClass(), event.eventId());
    }

    for (Consumer<DomainEvent> globalHandler : new ArrayList<>(globalSubscribers)) {
      try {
        globalHandler.accept(event);
      } catch (Exception e) {
        log.error(
          "Error processing global event {} with handler for type {} : {}",
          event.eventId(),
          eventType.getSimpleName(),
          e.getMessage()
        );
        //TODO
      }
    }
  }

  @Override
  public <T extends DomainEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
    subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    log.info("Subscribed to event: {}", eventType.getSimpleName());
  }

  @Override
  public void subscribeAll(Consumer<DomainEvent> handler) {
    globalSubscribers.add(handler);
    log.info("Subscribed to receive ALL domain events.");
  }
}
