package fr.rewrite.server.domain;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Result;
import org.openrewrite.SourceFile;

public interface RewriteEnginePort {
  List<SourceFile> parseSources(List<Path> sourcePaths, Path baseDir, Set<Path> classpath, ExecutionContext executionContext);

  List<Result> runRecipes(List<SourceFile> parsedSources, String recipeName, ExecutionContext executionContext); // NOUVEAU
}
