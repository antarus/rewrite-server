package fr.rewrite.server.infrastructure.controllers.datastore;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.domain.datastore.DatastoreManager;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rewrite")
class DatastoreController {

  private final DatastoreManager datastoreManager;

  public DatastoreController(DatastoreManager datastoreManager) {
    this.datastoreManager = datastoreManager;
  }

  @PostMapping("/datastore")
  public ResponseEntity<String> create(@RequestBody RestRepositoryUrl repositoryUrl) {
    RewriteId id = datastoreManager.createDatastore(RestRepositoryUrl.toDomain(repositoryUrl));

    return ResponseEntity.accepted().body(id.toString());
  }

  @GetMapping("/datastore/{uuid}")
  public ResponseEntity<RestDatastore> get(@PathVariable(value = "uuid") UUID uuid) {
    Datastore datastore = datastoreManager.getDatastore(RewriteId.from(uuid));

    return ResponseEntity.ok(RestDatastore.from(datastore));
  }
}
