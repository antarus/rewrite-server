package fr.rewrite.server.infrastructure.controllers;

import fr.rewrite.server.application.dto.RewriteConfig;
import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.feature.RewriteARepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rewrite")
public class RepoRewriteController {

  private final RewriteARepo rewriteARepo;

  public RepoRewriteController(RewriteARepo rewriteARepo) {
    this.rewriteARepo = rewriteARepo;
  }

  @PostMapping
  public ResponseEntity<String> submitJob(@RequestBody RewriteConfig config) {
    RewriteId id = rewriteARepo.createDatastore(config.repoUrl());

    return ResponseEntity.status(HttpStatus.ACCEPTED).body(id.toString());
  }
}
