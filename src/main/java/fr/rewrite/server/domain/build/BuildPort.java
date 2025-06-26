package fr.rewrite.server.domain.build;

import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.domain.exception.BuildToolException;

public interface BuildPort {
  void buildProject(Datastore datastore) throws BuildToolException;

//  Set<Path> getProjectClasspath(Path projectDir, Path outputPath, String mavenExecutablePath) throws BuildToolException;
}
