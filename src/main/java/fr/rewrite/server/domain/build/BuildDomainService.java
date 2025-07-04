package fr.rewrite.server.domain.build;


import fr.rewrite.server.domain.EventPublisher;
import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.build.command.BuildProject;
import fr.rewrite.server.domain.build.command.GetClassPath;
import fr.rewrite.server.domain.datastore.*;
import fr.rewrite.server.domain.datastore.event.DatastoreEvent;
import fr.rewrite.server.domain.datastore.event.DatastoreStatusChanged;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentState;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentStateRepository;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.StatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Set;

@DomainService
public class BuildDomainService {

    private static final Logger log = LoggerFactory.getLogger(BuildDomainService.class);

    private final DatastoreEventStore eventStore;
    private final EventPublisher<DatastoreEvent> eventPublisher;
    private final BuildPort buildPort;
    private final DatastoreCurrentStateRepository datastoreCurrentStateRepository;
private final DatastoreDomainService  datastoreDomainService;
    public BuildDomainService(DatastoreEventStore eventStore,
                              EventPublisher<DatastoreEvent> eventPublisher,
                              DatastoreCurrentStateRepository datastoreCurrentStateRepository,
                              BuildPort buildPort, DatastoreDomainService datastoreDomainService) {
        this.eventStore = eventStore;
        this.eventPublisher = eventPublisher;
        this.buildPort = buildPort;
        this.datastoreCurrentStateRepository=datastoreCurrentStateRepository;

        this.datastoreDomainService = datastoreDomainService;
    }

    public void buildProject(BuildProject cmd) {
        transitionToState(cmd.datastoreId(),StatusEnum.BUILDING);
        try {
            buildPort.buildProject(cmd.datastoreId());
            transitionToState(cmd.datastoreId(),StatusEnum.BUILD);
        }catch (Exception e) {
            transitionToState(cmd.datastoreId(),StatusEnum.BUILD_FAILED);
            throw e;
        }

    }
    public void getClassPath(GetClassPath cmd) {
        transitionToState(cmd.datastoreId(),StatusEnum.CLASSPATH_GETTING);
        try {
            Set<Path> classPath = buildPort.getClassPath(cmd.datastoreId());
            if (classPath==null) {
                classPath= Set.of();
            }
            datastoreDomainService.saveObjectToCache(cmd.datastoreId(), "classpath", DatastoreClasspath.from(cmd.datastoreId(), classPath));
            transitionToState(cmd.datastoreId(),StatusEnum.CLASSPATH);
        }catch (Exception e) {
            transitionToState(cmd.datastoreId(),StatusEnum.CLASSPATH_FAILED);
            throw e;
        }

    }



    private void transitionToState(DatastoreId datastoreId, StatusEnum nextStatus) {
        DatastoreCurrentState currentState =         datastoreCurrentStateRepository
                .get(datastoreId)
                .orElseThrow(() -> new DatastoreNotFoundException(datastoreId));

        log.trace("transitionToState datastoreId: {} from: {} to {}",datastoreId, currentState.lastStatus(), nextStatus);
        if (!currentState.lastStatus().canTransitionTo(nextStatus)) {
            throw new InvalidStateTransitionException(datastoreId, currentState.lastStatus(), nextStatus);
        }
        SequenceId nextNumber= eventStore.nextSequenceId(datastoreId);
        log.debug("transitionToState datastoreId: {} nextNumber: {}",datastoreId,nextNumber);
        DatastoreStatusChanged event=  DatastoreStatusChanged.from(datastoreId, nextStatus,nextNumber );
        eventStore.save(event);
        eventPublisher.publish(event);
    }
}
