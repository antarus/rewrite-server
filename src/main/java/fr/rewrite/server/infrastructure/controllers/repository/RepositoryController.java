package fr.rewrite.server.infrastructure.controllers.repository;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.domain.datastore.DatastoreManager;
import fr.rewrite.server.domain.repository.RepositoryManager;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.infrastructure.controllers.datastore.RestDatastore;
import fr.rewrite.server.infrastructure.controllers.datastore.RestRepositoryUrl;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rewrite")
class RepositoryController {

  private final RepositoryManager repositoryManager;

  public RepositoryController(RepositoryManager repositoryManager) {
    this.repositoryManager = repositoryManager;
  }

  @PostMapping("/repository/clone")
  public ResponseEntity<String> create(@RequestBody RestRepositoryEntry repositoryEntry) {
    repositoryManager.cloneRepository(RepositoryURL.from(repositoryEntry.url()), RestCredentials.toDomain(repositoryEntry.credential()));

    return ResponseEntity.accepted().build();
  }
}
