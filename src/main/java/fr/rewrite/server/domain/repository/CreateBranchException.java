package fr.rewrite.server.domain.repository;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class CreateBranchException extends RewriteServerException {

  public CreateBranchException(RewriteId rewriteId, String branchName, Exception cause) {
    super(
      badRequest(RepositoryErrorKey.BRANCH_CREATION_ERROR)
        .cause(cause)
        .addParameter("id", rewriteId.get().toString())
        .addParameter("branchName", branchName)
    );
  }
}
