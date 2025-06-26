package fr.rewrite.server.domain.build;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.domain.datastore.DatastoreManager;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.exception.BuildToolException;
import fr.rewrite.server.domain.repository.InvalidStateTransitionException;
import fr.rewrite.server.domain.spi.JobPort;
import fr.rewrite.server.domain.state.State;
import fr.rewrite.server.domain.state.StateEnum;
import fr.rewrite.server.domain.state.StateNotFoundException;
import fr.rewrite.server.domain.state.StateRepository;

@DomainService
public class BuildManagerImpl implements BuildManager {
    private final JobPort jobPort;
    private final DatastoreManager datastoreManager;
    private final StateRepository stateRepository;

    public BuildManagerImpl(JobPort jobPort, DatastoreManager datastoreManager, StateRepository stateRepository) {
        this.jobPort = jobPort;
        this.datastoreManager = datastoreManager;
        this.stateRepository = stateRepository;
    }

    @Override
    public void buildProject(RewriteId rewriteId) throws BuildToolException {
        Datastore datastore = datastoreManager.getDatastore(rewriteId);
        transitionToState(rewriteId, StateEnum.BUILDING);
        jobPort.buildProject(datastore);
    }

    private void transitionToState(RewriteId rewriteId, StateEnum nextStatus) {
        State currentState = stateRepository.get(rewriteId).orElseThrow(() -> new StateNotFoundException(rewriteId));

        if (!currentState.status().canTransitionTo(nextStatus)) {
            throw new InvalidStateTransitionException(rewriteId, currentState.status(), nextStatus);
        }

        stateRepository.save(currentState.withStatus(nextStatus));
    }
}
