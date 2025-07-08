package fr.rewrite.server.domain.updater;

import fr.rewrite.server.domain.EventPublisher;
import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.StatusEnum;
import fr.rewrite.server.domain.build.BuildPort;
import fr.rewrite.server.domain.build.command.BuildProject;
import fr.rewrite.server.domain.build.command.GetClassPath;
import fr.rewrite.server.domain.datastore.*;
import fr.rewrite.server.domain.datastore.event.DatastoreEvent;
import fr.rewrite.server.domain.datastore.event.DatastoreStatusChanged;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentState;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentStateRepository;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.updater.command.UpdaterCommand;
import java.nio.file.Path;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DomainService
public class UpdaterDomainService {

  private static final Logger log = LoggerFactory.getLogger(UpdaterDomainService.class);

  private final DatastoreEventStore eventStore;
  private final EventPublisher<DatastoreEvent> eventPublisher;
  private final BuildPort buildPort;
  private final DatastoreCurrentStateRepository datastoreCurrentStateRepository;
  private final DatastoreDomainService datastoreDomainService;

  public UpdaterDomainService(
    DatastoreEventStore eventStore,
    EventPublisher<DatastoreEvent> eventPublisher,
    DatastoreCurrentStateRepository datastoreCurrentStateRepository,
    BuildPort buildPort,
    DatastoreDomainService datastoreDomainService
  ) {
    this.eventStore = eventStore;
    this.eventPublisher = eventPublisher;
    this.buildPort = buildPort;
    this.datastoreCurrentStateRepository = datastoreCurrentStateRepository;

    this.datastoreDomainService = datastoreDomainService;
  }

  public void updateProject(UpdaterCommand cmd) {
    transitionToState(cmd.datastoreId(), StatusEnum.UPDATING);
    try {
      buildPort.buildProject(cmd.datastoreId());
      transitionToState(cmd.datastoreId(), StatusEnum.UPDATED);
    } catch (Exception e) {
      transitionToState(cmd.datastoreId(), StatusEnum.UPDATE_FAILED);
      throw e;
    }
  }

  private void transitionToState(DatastoreId datastoreId, StatusEnum nextStatus) {
    DatastoreCurrentState currentState = datastoreCurrentStateRepository
      .get(datastoreId)
      .orElseThrow(() -> new DatastoreNotFoundException(datastoreId));

    log.trace("transitionToState datastoreId: {} from: {} to {}", datastoreId, currentState.lastStatus(), nextStatus);
    if (!currentState.lastStatus().canTransitionTo(nextStatus)) {
      throw new InvalidStateTransitionException(datastoreId, currentState.lastStatus(), nextStatus);
    }
    SequenceId nextNumber = eventStore.nextSequenceId(datastoreId);
    log.debug("transitionToState datastoreId: {} nextNumber: {}", datastoreId, nextNumber);
    DatastoreStatusChanged event = DatastoreStatusChanged.from(datastoreId, nextStatus, nextNumber);
    eventStore.save(event);
    eventPublisher.publish(event);
  }
}
