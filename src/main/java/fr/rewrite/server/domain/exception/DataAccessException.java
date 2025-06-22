package fr.rewrite.server.domain.exception;

@Deprecated
public class DataAccessException extends RuntimeException {

  public DataAccessException(String message, Throwable cause) {
    super(message, cause);
  }
}
