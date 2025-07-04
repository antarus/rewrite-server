package fr.rewrite.server.domain.repository;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class CreateBranchException extends RewriteServerException {

  public CreateBranchException(DatastoreId datastoreId, String branchName, Exception cause) {
    super(
      badRequest(RepositoryErrorKey.BRANCH_CREATION_ERROR)
        .cause(cause)
        .addParameter("id", datastoreId.get().toString())
        .addParameter("branchName", branchName)
    );
  }
}
