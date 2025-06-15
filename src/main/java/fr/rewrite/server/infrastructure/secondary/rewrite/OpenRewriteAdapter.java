package fr.rewrite.server.infrastructure.secondary.rewrite;

import fr.rewrite.server.domain.RewriteEnginePort;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.openrewrite.ExecutionContext;
import org.openrewrite.LargeSourceSet;
import org.openrewrite.Recipe;
import org.openrewrite.Result;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.InMemoryLargeSourceSet;
import org.openrewrite.java.JavaParser;
import org.openrewrite.marker.BuildTool; // Importez BuildTool
import org.openrewrite.marker.Markers; // Importez Markers
import org.springframework.stereotype.Component;

@Component
public class OpenRewriteAdapter implements RewriteEnginePort {

  @Override
  public List<SourceFile> parseSources(List<Path> sourcePaths, Path baseDir, Set<Path> classpath, ExecutionContext executionContext) {
    JavaParser javaParser = JavaParser.fromJavaVersion().classpath(classpath).build();

    // Ajout du marqueur BuildTool directement sur les sources après le parsing
    // Le BuildTool est un marqueur, pas une propriété de l'ExecutionContext global.
    // On l'ajoutera aux SourceFiles pour qu'il soit persistant.
    // Pour l'exécution, il est souvent implicite ou ajouté par des plugins Maven/Gradle.
    // Ici, on ne le met plus directement dans le contexte de cette manière.

    List<SourceFile> parsedSources = javaParser.parse(sourcePaths, baseDir, executionContext).collect(Collectors.toList());

    // Si une recette a besoin du BuildTool, elle l'ajoutera elle-même si elle en a besoin
    // ou vous pouvez ajouter ce marqueur à chaque SourceFile ici, si c'est une exigence forte
    // pour toutes les recettes, mais ce n'est pas le comportement par défaut.

    return parsedSources;
  }

  @Override
  public List<Result> runRecipes(List<SourceFile> parsedSources, String recipeName, ExecutionContext executionContext) {
    try {
      // Instancier la recette dynamiquement par son nom
      Class<?> recipeClass = Class.forName(recipeName);
      if (!Recipe.class.isAssignableFrom(recipeClass)) {
        throw new IllegalArgumentException("Class '" + recipeName + "' is not an OpenRewrite Recipe.");
      }
      Recipe recipe = (Recipe) recipeClass.getDeclaredConstructor().newInstance();

      // Créer un LargeSourceSet à partir des sources parsées
      LargeSourceSet largeSourceSet = new InMemoryLargeSourceSet(parsedSources);

      // Exécuter la recette en passant le LargeSourceSet
      return recipe.run(largeSourceSet, executionContext).getChangeset().getAllResults();
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Recipe not found: " + recipeName, e);
    } catch (Exception e) {
      throw new RuntimeException("Failed to run recipe '" + recipeName + "': " + e.getMessage(), e);
    }
  }
}
