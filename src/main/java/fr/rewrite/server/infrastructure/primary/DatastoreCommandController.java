package fr.rewrite.server.infrastructure.primary;

import fr.rewrite.server.application.DatastoreApplicationService;
import fr.rewrite.server.domain.datastore.command.DatastoreCreation;
import fr.rewrite.server.domain.repository.RepositoryURL;
import jakarta.validation.constraints.NotBlank;
import java.util.concurrent.ExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/datastore")
class DatastoreCommandController {

  private final DatastoreApplicationService dataStore;

  DatastoreCommandController(DatastoreApplicationService dataStore) {
    this.dataStore = dataStore;
  }

  @PostMapping
  ResponseEntity<Void> create(@RequestBody RestRepositoryUrl repositoryUrl) throws ExecutionException, InterruptedException {
    dataStore.create(new DatastoreCreation(repositoryUrl.toDomain()));

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  record RestRepositoryUrl(@NotBlank String url) {
    public RepositoryURL toDomain() {
      return new RepositoryURL(url);
    }
  }
}
