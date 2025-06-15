package fr.rewrite.server.domain.exception;

// On peut choisir d'étendre RuntimeException pour une exception non vérifiée,
// ou Exception pour une exception vérifiée.
// Pour les adaptateurs d'infrastructure, RuntimeException est souvent préférée
// pour éviter la propagation verbale de 'throws' dans les couches supérieures,
// mais elle doit être documentée ou catchée à un point d'entrée.
public class GitOperationException extends RuntimeException {

  public GitOperationException(String message) {
    super(message);
  }

  public GitOperationException(String message, Throwable cause) {
    super(message, cause);
  }
}
