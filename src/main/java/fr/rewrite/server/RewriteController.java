package fr.rewrite.server;

import fr.rewrite.server.application.RewriteOrchestrator;
import fr.rewrite.server.application.dto.RewriteConfig;
import fr.rewrite.server.domain.exception.GitOperationException;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController // Indique que c'est un contrôleur REST
@RequestMapping("/api/rewrite") // Chemin de base pour toutes les requêtes de ce contrôleur
public class RewriteController {

  private final RewriteOrchestrator rewriteOrchestrator;

  // Injection de l'orchestrateur par Spring
  public RewriteController(RewriteOrchestrator rewriteOrchestrator) {
    this.rewriteOrchestrator = rewriteOrchestrator;
  }

  @PostMapping // Gère les requêtes POST vers /api/rewrite
  public ResponseEntity<String> startRewrite(@RequestBody RewriteConfig config) {
    try {
      System.out.println(config);
      // Lancer le processus de réécriture
      rewriteOrchestrator.runRewriteProcess(config);
      return ResponseEntity.ok("Rewrite process started successfully.");
    } catch (GitOperationException e) {
      // Exceptions métier liées à Git -> 400 Bad Request
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Git operation failed: " + e.getMessage(), e);
    } catch (IllegalArgumentException e) {
      // Erreurs de validation des arguments, recettes introuvables, etc.
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid configuration: " + e.getMessage(), e);
    } catch (Exception e) {
      // Toute autre exception non gérée -> 500 Internal Server Error
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + e.getMessage(), e);
    }
  }
  // Vous pouvez ajouter d'autres endpoints pour le statut, les logs, etc.
  // Pour les opérations longues, il est recommandé d'implémenter un système de Job ID
  // où le serveur renvoie un ID de tâche, et le client peut ensuite interroger
  // /api/rewrite/status/{jobId} pour obtenir l'avancement.
}
