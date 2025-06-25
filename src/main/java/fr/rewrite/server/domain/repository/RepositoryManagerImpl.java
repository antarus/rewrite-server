package fr.rewrite.server.domain.repository;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.spi.JobPort;
import fr.rewrite.server.domain.state.State;
import fr.rewrite.server.domain.state.StateRepository;
import java.util.Optional;

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
    Optional<State> optState = stateRepository.get(RewriteId.from(repositoryURL));
    if (optState.isEmpty()) {}

    if (credential == null) {
      jobPort.cloneRepository(repositoryURL);
    } else {
      jobPort.cloneRepository(repositoryURL, credential);
    }
  }
}
