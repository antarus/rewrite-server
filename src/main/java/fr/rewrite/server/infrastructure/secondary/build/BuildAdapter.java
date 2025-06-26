package fr.rewrite.server.infrastructure.secondary.build;

import fr.rewrite.server.domain.build.BuildPort;
import fr.rewrite.server.domain.build.ProjectType;
import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.domain.exception.BuildToolException;
import fr.rewrite.server.domain.state.RewriteConfig;
import fr.rewrite.server.shared.error.domain.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class BuildAdapter implements BuildPort {


  private static final Logger log = LoggerFactory.getLogger(BuildAdapter.class);
  private final RewriteConfig rewriteConfig;
  private final Tool mavenBuildTool;

  public BuildAdapter(RewriteConfig rewriteConfig, Tool mavenBuildTool) {
    this.rewriteConfig = rewriteConfig;
      this.mavenBuildTool = mavenBuildTool;
  }

  @Override
  public void buildProject(Datastore datastore) throws BuildToolException {
    Assert.notNull("datastore", datastore);
    ProjectType projectType = getProjectType(rewriteConfig.resolve(datastore.rewriteId()));

if (projectType.equals(ProjectType.MAVEN)) {
  mavenBuildTool.build(datastore);
}
// TODO other build tool


  }

  private ProjectType getProjectType(Path path) {
    // TODO add recursivity for multi module

    if (!java.nio.file.Files.exists(path) || !java.nio.file.Files.isDirectory(path)) {
      log.info("The path doesn't exist or is not a directory : {}", path);
      return ProjectType.UNKNOWN;
    }

    if (java.nio.file.Files.exists(path.resolve("pom.xml"))) {
      return ProjectType.MAVEN;
    }

    if (java.nio.file.Files.exists(path.resolve("build.gradle")) ||
            java.nio.file.Files.exists(path.resolve("build.gradle.kts"))) {
      return ProjectType.GRADLE;
    }

    if (java.nio.file.Files.exists(path.resolve("build.xml"))) {
      return ProjectType.ANT;
    }

    return ProjectType.UNKNOWN;
  }

}
