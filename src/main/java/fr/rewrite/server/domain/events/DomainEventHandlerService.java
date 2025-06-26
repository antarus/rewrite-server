package fr.rewrite.server.domain.events;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.build.BuildFinishedEvent;
import fr.rewrite.server.domain.datastore.DatastoreCreatedEvent;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.repository.BranchCreatedEvent;
import fr.rewrite.server.domain.repository.RepositoryClonedEvent;
import fr.rewrite.server.domain.state.StateEnum;
import fr.rewrite.server.domain.state.StateRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DomainService
public class DomainEventHandlerService {

  private final Map<Class<?>, Consumer<DomainEvent>> handlers = new HashMap<>();
  private static final Logger log = LoggerFactory.getLogger(DomainEventHandlerService.class);
  private final StateRepository stateRepository;

  public DomainEventHandlerService(StateRepository stateRepository) {
    this.stateRepository = stateRepository;
    initHandlers();
  }

  private void initHandlers() {
    registerHandler(DatastoreCreatedEvent.class, this::handleDatastoreCreatedEvent);
    registerHandler(RepositoryClonedEvent.class, this::handleRepositoryClonedEvent);
    registerHandler(BranchCreatedEvent.class, this::handleBranchCreatedEvent);
    registerHandler(BuildFinishedEvent.class, this::handleBuildFinishedEvent);
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
        log.error("Error processing event {} of type {} : {}", event.eventId(), event.getClass().getSimpleName(), e.getMessage());
        //TODO
      }
    } else {
      log.info("DomainEventHandlerService: No specific handler registered for event type: {}", event.getClass().getSimpleName());
    }
  }

  private void handleDatastoreCreatedEvent(DatastoreCreatedEvent event) {
    updateState(event.rewriteId(), StateEnum.DATASTORE_CREATED);
  }

  private void handleRepositoryClonedEvent(RepositoryClonedEvent event) {
    updateState(event.rewriteId(), StateEnum.CLONED);
  }

  private void handleBranchCreatedEvent(BranchCreatedEvent event) {
    updateState(event.rewriteId(), StateEnum.BRANCH_CREATED);
  }

  private void handleBuildFinishedEvent(BuildFinishedEvent event) {
    updateState(event.rewriteId(), StateEnum.BUILD);
  }

  private void updateState(RewriteId rewriteId, StateEnum newStatus) {
    stateRepository.get(rewriteId).map(state -> state.withStatus(newStatus)).ifPresent(stateRepository::save);
  }
}
