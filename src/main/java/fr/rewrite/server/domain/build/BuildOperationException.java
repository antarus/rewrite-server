package fr.rewrite.server.domain.build;

import fr.rewrite.server.shared.error.domain.ErrorStatus;
import fr.rewrite.server.shared.error.domain.RewriteServerException;

public class BuildOperationException extends RewriteServerException {

  public BuildOperationException(String message) {
    super(internalServerError(BuildErrorKey.BUILD_OPERATION_ERROR).message(message));
  }
  public BuildOperationException(Throwable cause) {
    super(internalServerError(BuildErrorKey.BUILD_OPERATION_ERROR) .cause(cause));
  }
  public BuildOperationException(String message, Throwable cause) {
    super(internalServerError(BuildErrorKey.BUILD_OPERATION_ERROR)
            .message(message)
            .cause(cause)
            .status(ErrorStatus.INTERNAL_SERVER_ERROR));
  }
}
