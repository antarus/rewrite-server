package fr.rewrite.server.domain.repository;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.spi.JobPort;
import fr.rewrite.server.domain.state.State;
import fr.rewrite.server.domain.state.StateEnum;
import fr.rewrite.server.domain.state.StateNotFoundException;
import fr.rewrite.server.domain.state.StateRepository;

@DomainService
public class RepositoryManagerImpl implements RepositoryManager {

  private final JobPort jobPort;
  private final StateRepository stateRepository;

  public RepositoryManagerImpl(JobPort jobPort, StateRepository stateRepository) {
    this.jobPort = jobPort;
    this.stateRepository = stateRepository;
  }

  @Override
  public void cloneRepository(RepositoryURL repositoryURL, Credentials credential) {
    RewriteId rewriteId = RewriteId.from(repositoryURL);
    transitionToState(rewriteId, StateEnum.CLONING);

    if (credential == null) {
      jobPort.cloneRepository(repositoryURL);
    } else {
      jobPort.cloneRepository(repositoryURL, credential);
    }
  }

  @Override
  public void createBranch(RewriteId rewriteId, String branchName) {
    transitionToState(rewriteId, StateEnum.BRANCH_CREATING);
    jobPort.createBranch(rewriteId, branchName);
  }

  private void transitionToState(RewriteId rewriteId, StateEnum nextStatus) {
    State currentState = stateRepository.get(rewriteId).orElseThrow(() -> new StateNotFoundException(rewriteId));

    if (!currentState.status().canTransitionTo(nextStatus)) {
      throw new InvalidStateTransitionException(rewriteId, currentState.status(), nextStatus);
    }

    stateRepository.save(currentState.withStatus(nextStatus));
  }
}
