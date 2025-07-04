package fr.rewrite.server.infrastructure.primary;

import fr.rewrite.server.application.BuildApplicationService;
import fr.rewrite.server.domain.build.command.BuildProject;
import fr.rewrite.server.domain.build.command.GetClassPath;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/build")
class BuildCommandController {

  private final BuildApplicationService buildApplicationService;

  BuildCommandController(BuildApplicationService buildApplicationService) {
    this.buildApplicationService = buildApplicationService;
  }

  @PostMapping("/project")
  public ResponseEntity<Void> buildProject(@RequestBody RestDatastoreId datastoreId) {
    buildApplicationService.buildProject(BuildProject.from(datastoreId.toDomain()));

    return ResponseEntity.accepted().build();
  }

  @PostMapping("/classpath")
  public ResponseEntity<Void> getClasspath(@RequestBody RestDatastoreId datastoreId) {
    buildApplicationService.getClasspath(GetClassPath.from(datastoreId.toDomain()));

    return ResponseEntity.ok().build();
  }
}
