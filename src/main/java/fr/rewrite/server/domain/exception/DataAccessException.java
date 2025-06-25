package fr.rewrite.server.domain.exception;

/**
 * @deprecated use new rewriteException
 */
@Deprecated(forRemoval = true, since = "0.1")
public class DataAccessException extends RuntimeException {

  public DataAccessException(String message, Throwable cause) {
    super(message, cause);
  }
}
