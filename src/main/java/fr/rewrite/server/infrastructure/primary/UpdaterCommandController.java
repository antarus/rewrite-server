package fr.rewrite.server.infrastructure.primary;

import fr.rewrite.server.application.UpdaterApplicationService;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.updater.Recipe;
import fr.rewrite.server.domain.updater.command.UpdaterCommand;
import fr.rewrite.server.shared.error.domain.Assert;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/updater")
class UpdaterCommandController {

  private final UpdaterApplicationService updaterApplicationService;

  UpdaterCommandController(UpdaterApplicationService updaterApplicationService) {
    this.updaterApplicationService = updaterApplicationService;
  }

  @PostMapping
  public ResponseEntity<Void> updateProject(@RequestBody RestUpdaterCommand datastoreId) {
    updaterApplicationService.updateProject(datastoreId.toDomain());

    return ResponseEntity.accepted().build();
  }
}

record RestUpdaterCommand(DatastoreId dsId, Recipe recipe) {
  public RestUpdaterCommand {
    Assert.notNull("recipe", recipe);
    Assert.notNull("dsId", dsId);
  }
  public UpdaterCommand toDomain() {
    return new UpdaterCommand(dsId(), recipe());
  }
}
