package fr.rewrite.server.domain.events;

import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.repository.RepositoryCreatedEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DomainService
public class DomainEventHandlerService {

  private final Map<Class<? extends DomainEvent>, Consumer<DomainEvent>> handlers = new HashMap<>();

  private static final Logger log = LoggerFactory.getLogger(DomainEventHandlerService.class);

  public DomainEventHandlerService() {
    initHandlers();
  }

  private void initHandlers() {
    registerHandler(RepositoryCreatedEvent.class, this::handleRepositoryCreatedEvent);
    registerHandler(LoggingEvent.class, this::handleLoggingEvent);
  }

  <T extends DomainEvent> void registerHandler(Class<T> eventType, Consumer<T> handler) {
    @SuppressWarnings("unchecked")
    Consumer<DomainEvent> castedHandler = (Consumer<DomainEvent>) handler;
    handlers.put(eventType, castedHandler);
  }

  public void handleEvent(DomainEvent event) {
    Consumer<DomainEvent> handler = handlers.get(event.getClass());

    if (handler != null) {
      try {
        log.debug("DomainEventHandlerService: Dispatching event of type: {}", event.getClass().getSimpleName());
        handler.accept(event);
      } catch (Exception e) {
        log.error("Error processing event {}  of type {} : {}", event.eventId(), event.getClass().getSimpleName(), e.getMessage());
        //TODO
      }
    } else {
      log.info("DomainEventHandlerService: No specific handler registered for event type: {}", event.getClass().getSimpleName());
    }
  }

  void handleRepositoryCreatedEvent(RepositoryCreatedEvent event) {
    log.info("DomainEventHandlerService: Handling RepositoryCreatedEvent : {}", event.path());
  }

  void handleLoggingEvent(LoggingEvent event) {
    log.info("DomainEventHandlerService: Handling LoggingEvent : {}", event.log());
  }
}
