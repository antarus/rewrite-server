package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.repository.RepositoryURL;

public interface DatastoreManager {
  RewriteId createDatastore(RepositoryURL repoUrl);
  Datastore getDatastore(RewriteId repoUrl);
}
