package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class DatastoreAlreadyExistException extends RewriteServerException {

  public DatastoreAlreadyExistException(Datastore datastore, RepositoryURL repositoryURL) {
    super(
      badRequest(DatastoreErrorKey.DATASTORE_ALREADY_EXIST)
        .addParameter("id", datastore.rewriteId().get().toString())
        .addParameter("repository.url", repositoryURL.url())
        .addParameter("datastore.path", datastore.path().toString())
    );
  }
}
