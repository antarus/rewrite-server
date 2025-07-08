package fr.rewrite.server.infrastructure.primary;

import fr.rewrite.server.application.DatastoreApplicationService;
import fr.rewrite.server.domain.StatusEnum;
import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.datastore.DatastoreNotFoundException;
import fr.rewrite.server.domain.datastore.event.DatastoreEvent;
import fr.rewrite.server.domain.datastore.event.DatastoreHistory;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentState;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.error.domain.Assert;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/datastore")
class DatastoreQueryController {

  private final DatastoreApplicationService dataStore;

  DatastoreQueryController(DatastoreApplicationService dataStore) {
    this.dataStore = dataStore;
  }

  @GetMapping("/{uuid}")
  public ResponseEntity<RestDatastoreCurrentState> get(@PathVariable(value = "uuid") UUID uuid) {
    DatastoreId datastoreId = new DatastoreId(uuid);
    RestDatastoreCurrentState currentState = dataStore
      .getCurrentState(datastoreId)
      .map(RestDatastoreCurrentState::fromDomain)
      .orElseThrow(() -> new DatastoreNotFoundException(datastoreId));
    return ResponseEntity.ok(currentState);
  }

  @GetMapping
  public ResponseEntity<Collection<RestDatastoreCurrentState>> getAllDatastore() {
    return ResponseEntity.ok(dataStore.findAllCurrentStates().stream().map(RestDatastoreCurrentState::fromDomain).toList());
  }

  @GetMapping("/{uuid}/history")
  public ResponseEntity<RestDatastoreHistory> getHistory(@PathVariable(value = "uuid") UUID uuid) {
    DatastoreId datastoreId = new DatastoreId(uuid);
    return ResponseEntity.ok(RestDatastoreHistory.fromDomain(dataStore.getHistory(datastoreId)));
  }

  record RestDatastoreHistory(RestDatastoreCreated start, List<DatastoreEvent> followingEvents) {
    public static RestDatastoreHistory fromDomain(DatastoreHistory history) {
      return new RestDatastoreHistory(RestDatastoreCreated.from(history), new LinkedList<>(history.followingEvents()));
      // TODO how manage copy of List<DatastoreEvent> to List<RestDatastoreEvent>
    }
  }

  record RestDatastoreCreated(
    RestDatastoreId datastoreId,
    RestRepositoryURL repositoryURL,
    RestSequenceId sequenceId,
    UUID eventId,
    Instant occurredOn
  )
    implements RestDatastoreEvent {
    public static RestDatastoreCreated from(DatastoreHistory history) {
      return new RestDatastoreCreated(
        RestDatastoreId.fromDomain(history.start().datastoreId()),
        RestRepositoryURL.from(history.start().repositoryURL().url()),
        RestSequenceId.from(history.start().sequenceId()),
        history.start().eventId(),
        history.start().occurredOn()
      );
    }
  }

  record RestDatastoreCurrentState(DatastoreId datastoreId, RepositoryURL repositoryURL, StatusEnum state, boolean deleted) {
    public static RestDatastoreCurrentState fromDomain(DatastoreCurrentState datastoreCurrentState) {
      return new RestDatastoreCurrentState(
        datastoreCurrentState.datastoreId(),
        datastoreCurrentState.repositoryURL(),
        datastoreCurrentState.lastStatus(),
        false
      );
    }
  }

  record RestDatastore(UUID rewriteId) {
    public RestDatastore {
      Assert.notNull("rewriteId", rewriteId);
    }
    public static RestDatastore from(Datastore datastore) {
      return new RestDatastore(datastore.datastoreId().uuid());
    }
  }
}
