package fr.rewrite.server.infrastructure.controllers.build;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.build.BuildManager;
import fr.rewrite.server.infrastructure.controllers.datastore.RestDatastore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rewrite")
class BuildController {

  private final BuildManager buildManager;

  public BuildController(BuildManager buildManager) {

      this.buildManager = buildManager;
  }

  @GetMapping("/build/{uuid}")
  public ResponseEntity<Void> get(@PathVariable(value = "uuid") UUID uuid) {
    buildManager.buildProject(RewriteId.from(uuid));

    return ResponseEntity.ok().build();
  }
}
