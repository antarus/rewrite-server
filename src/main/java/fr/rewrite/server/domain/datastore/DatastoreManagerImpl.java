package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.domain.spi.JobPort;
import fr.rewrite.server.domain.state.State;
import fr.rewrite.server.domain.state.StateRepository;
import fr.rewrite.server.shared.error.domain.Assert;
import java.util.Optional;

@DomainService
public class DatastoreManagerImpl implements DatastoreManager {

  private final JobPort jobPort;
  private final StateRepository stateRepository;
  private final DatastoreWorker datastoreWorker;

  public DatastoreManagerImpl(JobPort jobPort, StateRepository stateRepository, DatastoreWorker datastoreWorker) {
    Assert.notNull("stateRepository", stateRepository);
    Assert.notNull("jobPort", jobPort);
    Assert.notNull("datastoreWorker", datastoreWorker);
    this.datastoreWorker = datastoreWorker;
    this.stateRepository = stateRepository;
    this.jobPort = jobPort;
  }

  @Override
  public RewriteId createDatastore(RepositoryURL repositoryURL) {
    Assert.notNull("repositoryURL", repositoryURL);
    RewriteId rewriteId = RewriteId.from(repositoryURL);
    Optional<State> optState = stateRepository.get(rewriteId);
    if (optState.isEmpty()) {
      stateRepository.save(State.init(rewriteId));
      jobPort.createDatastoreJob(rewriteId);
    } else {
      Datastore datastore = this.getDatastore(rewriteId);
      throw new DatastoreAlreadyExistException(datastore, repositoryURL);
    }

    return rewriteId;
  }

  @Override
  public Datastore getDatastore(RewriteId rewriteId) {
    Optional<State> optState = stateRepository.get(rewriteId);
    if (optState.isEmpty()) {
      throw new DatastoreNotFoundException(rewriteId);
    } else {
      return datastoreWorker.getDatastore(rewriteId);
    }
  }
}
