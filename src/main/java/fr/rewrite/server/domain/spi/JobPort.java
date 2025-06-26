package fr.rewrite.server.domain.spi;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.domain.repository.Credentials;
import fr.rewrite.server.domain.repository.RepositoryURL;

public interface JobPort {
  void createDatastoreJob(RewriteId rewriteId);
  void cloneRepository(RepositoryURL repositoryURL, Credentials credential);
  void cloneRepository(RepositoryURL repositoryURL);

  void createBranch(RewriteId rewriteId, String currentBranch);

  void buildProject(Datastore datastore);
}
