package fr.rewrite.server.infrastructure.primary;

import fr.rewrite.server.application.RepositoryApplicationService;
import fr.rewrite.server.domain.repository.Credentials;
import fr.rewrite.server.domain.repository.RepositoryBranchName;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.domain.repository.command.RepositoryBranchCreate;
import fr.rewrite.server.domain.repository.command.RepositoryClone;
import fr.rewrite.server.domain.repository.command.RepositoryDelete;
import fr.rewrite.server.shared.error.domain.Assert;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/repository")
class RepositoryCommandController {

  private final RepositoryApplicationService repository;

  RepositoryCommandController(RepositoryApplicationService repository) {
    this.repository = repository;
  }

  @PostMapping("/clone")
  public ResponseEntity<String> create(@RequestBody RestRepositoryEntry repositoryEntry) {
    repository.cloneRepository(
      RepositoryClone.from(
        repositoryEntry.datastoreId().toDomain(),
        RepositoryURL.from(repositoryEntry.url()),
        RestCredentials.toDomain(repositoryEntry.credential())
      )
    );

    return ResponseEntity.accepted().build();
  }

  @DeleteMapping("/delete")
  public ResponseEntity<String> delete(@RequestBody RestRepositoryDelete repositoryEntry) {
    repository.deleteRepository(RepositoryDelete.from(repositoryEntry.datastoreId().toDomain()));

    return ResponseEntity.accepted().build();
  }

  @PostMapping("/branch")
  public ResponseEntity<Void> createBranchAndCheckout(@RequestBody RestBranchToCreate branchToCreate) {
    repository.createBranchAndCheckout(
      RepositoryBranchCreate.from(branchToCreate.datastoreId.toDomain(), RepositoryBranchName.from(branchToCreate.name()))
    );
    return ResponseEntity.accepted().build();
  }

  record RestBranchToCreate(RestDatastoreId datastoreId, String name) {
    public RestBranchToCreate {
      Assert.notNull("datastoreId", datastoreId);
      Assert.notNull("name", name);
    }
  }

  record RestCredentials(
    String username,
    // Personal Access Token
    String pat
  ) {
    public static Credentials toDomain(RestCredentials restCredentials) {
      if (restCredentials == null) {
        return null;
      }
      return new Credentials(restCredentials.username, restCredentials.pat);
    }
  }

  record RestRepositoryDelete(RestDatastoreId datastoreId) {
    RestRepositoryDelete {
      Assert.notNull("datastoreId", datastoreId);
    }
  }

  record RestRepositoryEntry(RestDatastoreId datastoreId, String url, RestCredentials credential) {
    RestRepositoryEntry {
      Assert.notNull("datastoreId", datastoreId);
      Assert.field("url", url).notBlank();
    }
  }
}
