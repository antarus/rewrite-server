package fr.rewrite.server.domain.exception;

public class FileSystemOperationException extends RuntimeException {

  public FileSystemOperationException(String message) {
    super(message);
  }

  public FileSystemOperationException(String message, Throwable cause) {
    super(message, cause);
  }
}
