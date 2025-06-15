package fr.rewrite.server.domain;

import fr.rewrite.server.domain.exception.BuildToolException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public interface BuildToolPort {
  void executeBuild(Path projectDir, String mavenExecutablePath) throws BuildToolException;

  Set<Path> getProjectClasspath(Path projectDir, Path outputPath, String mavenExecutablePath) throws BuildToolException;
}
