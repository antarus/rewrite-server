package fr.rewrite.server.domain.exception;

public class BuildToolException extends RuntimeException {

  public BuildToolException(String message) {
    super(message);
  }

  public BuildToolException(String message, Throwable cause) {
    super(message, cause);
  }
}
