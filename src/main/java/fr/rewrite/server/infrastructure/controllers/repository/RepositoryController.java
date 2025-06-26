package fr.rewrite.server.infrastructure.controllers.repository;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.repository.RepositoryManager;
import fr.rewrite.server.domain.repository.RepositoryURL;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

  @PostMapping("/repository/{id}/branch")
  public ResponseEntity<Void> createBranch(@PathVariable UUID id, @RequestBody RestBranchToCreate branchToCreate) {
    repositoryManager.createBranch(RewriteId.from(id), branchToCreate.name());
    return ResponseEntity.accepted().build();
  }
}
