package fr.rewrite.server;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Recipe;
// Changement ici : import de Recipe
//import org.openrewrite.RecipeDescriptor;
import org.openrewrite.config.Environment;
import org.openrewrite.config.OptionDescriptor;
import org.openrewrite.config.RecipeDescriptor;

public class RecipeLister {

  public static void main(String[] args) {
    System.out.println("Listing all available OpenRewrite recipes by category...");

    ExecutionContext ctx = new InMemoryExecutionContext();
    Environment env = Environment.builder().scanRuntimeClasspath().build();

    // --- Correction ici : env.listRecipes() retourne Collection<Recipe> ---
    Collection<Recipe> allRecipes = env.listRecipes();

    // Nous allons mapper les Recipes à leurs RecipeDescriptors pour la suite du traitement
    List<RecipeDescriptor> allRecipeDescriptors = allRecipes
      .stream()
      .map(Recipe::getDescriptor) // Convertit chaque Recipe en RecipeDescriptor
      .filter(rd -> !rd.getName().contains("test")) // Exclure les recettes de test
      .collect(Collectors.toList());

    // Tri et groupement par "catégorie" (basé sur le package de la recette)
    Map<String, List<RecipeDescriptor>> recipesByCategory = allRecipeDescriptors
      .stream() // Utilisation de allRecipeDescriptors
      .collect(
        Collectors.groupingBy(
          recipeDescriptor -> {
            String name = recipeDescriptor.getName();
            if (name.startsWith("org.openrewrite.")) {
              String[] parts = name.split("\\.");
              if (parts.length > 2) {
                String category = parts[2];
                if (
                  parts.length > 3 &&
                  (parts[3].equals("cleanup") ||
                    parts[3].equals("format") ||
                    parts[3].equals("migrate") ||
                    parts[3].equals("java") ||
                    parts[3].equals("spring") ||
                    parts[3].equals("maven") ||
                    parts[3].equals("xml") ||
                    parts[3].equals("yaml"))
                ) {
                  category += " " + parts[3];
                }
                return capitalize(category);
              }
            }
            return "General / Uncategorized";
          },
          Collectors.toList()
        )
      );

    // Affichage des recettes triées par catégorie et par nom
    recipesByCategory
      .entrySet()
      .stream()
      .sorted(Map.Entry.comparingByKey())
      .forEach(entry -> {
        String category = entry.getKey();
        List<RecipeDescriptor> recipes = entry.getValue();

        System.out.println("\n--- Category: " + category + " (" + recipes.size() + " recipes) ---");

        recipes
          .stream()
          .sorted(Comparator.comparing(RecipeDescriptor::getDisplayName))
          .forEach(recipeDescriptor -> {
            System.out.println("  " + recipeDescriptor.getDisplayName());
            System.out.println("    ID: " + recipeDescriptor.getName());
            System.out.println(
              "    Description: " +
              (recipeDescriptor.getDescription() != null ? recipeDescriptor.getDescription() : "No description available.")
            );

            Collection<OptionDescriptor> options = recipeDescriptor.getOptions();
            if (!options.isEmpty()) {
              System.out.println("    Options:");
              options.forEach(option -> {
                System.out.println("      - " + option.getName() + " (" + option.getType() + ")"); // Utiliser getNameOfType() pour le type
                System.out.println(
                  "        Description: " + (option.getDescription() != null ? option.getDescription() : "No description.")
                );
              });
            }
            System.out.println();
          });
      });
  }

  private static String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }
}
