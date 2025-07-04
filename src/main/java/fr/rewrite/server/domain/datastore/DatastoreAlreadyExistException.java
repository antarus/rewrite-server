package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class DatastoreAlreadyExistException extends RewriteServerException {

  public DatastoreAlreadyExistException(RepositoryURL repositoryURL) {
    super(
      badRequest(DatastoreErrorKey.DATASTORE_ALREADY_EXIST)
        .addParameter("id", DatastoreId.from(repositoryURL).get().toString())
        .addParameter("repository.url", repositoryURL.url())
    );
  }
}
