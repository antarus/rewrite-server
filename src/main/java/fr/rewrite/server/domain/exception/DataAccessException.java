package fr.rewrite.server.domain.exception;

public class DataAccessException extends RewriteException {

  public DataAccessException(String message) {
    super(badRequestBuilder(message));
  }

  public DataAccessException(String message, Throwable cause) {
    super(badRequestBuilder(message).cause(cause));
  }
}
