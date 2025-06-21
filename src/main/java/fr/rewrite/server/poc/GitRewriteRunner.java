package fr.rewrite.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
// Pour d'éventuels diffs avancés, non directement utilisé ici
import org.eclipse.jgit.lib.ObjectReader;
// Pour d'éventuels diffs avancés
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
// Pour d'éventuels diffs avancés
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
// Corrected import
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.openrewrite.*;
import org.openrewrite.config.Environment;
import org.openrewrite.internal.InMemoryLargeSourceSet;
import org.openrewrite.java.JavaParser;
import org.openrewrite.xml.XmlParser;
import org.openrewrite.yaml.YamlParser;

public class GitRewriteRunner {

  public static void main(String[] args) {
    // --- Configuration Générale ---
    String repoUrl = "https://github.com/openrewrite/spring-petclinic-migration"; // Utilisez une URL GitHub ou GitLab ici
    String localPathStr = "cloned-repo";
    Path localRepoDir = Paths.get(localPathStr);

    String recipeName = "org.openrewrite.java.format.AutoFormat";

    // --- Choisissez votre plateforme : "github" ou "gitlab" ---
    String platform = "github"; // Changez à "gitlab" si c'est le cas

    // --- Authentification Git (pour le clonage et le push) ---
    // Pour GitHub: utilisateur = votre nom d'utilisateur GitHub, PAT = votre PAT GitHub
    // Pour GitLab: utilisateur = "oauth2", PAT = votre PAT GitLab
    String gitUsername = "votre_nom_utilisateur_ou_oauth2";
    String gitPat = "VOTRE_PAT_GITHUB_OU_GITLAB_POUR_CLONE_PUSH";

    // --- Configuration Spécifique à la Plateforme pour les PR/MR ---
    String apiBaseUrl;
    String apiToken;
    String repoOwner; // Pour GitHub (ex: openrewrite)
    String repoName; // Pour GitHub (ex: rewrite-sample-project)
    String gitlabProjectId = null; // Pour GitLab (ID numérique)

    if ("github".equalsIgnoreCase(platform)) {
      apiBaseUrl = "https://api.github.com";
      apiToken = "ghp_VOTRE_PAT_GITHUB_POUR_API"; // Même PAT que pour Git si les scopes sont corrects
      // Extrait le propriétaire et le nom du dépôt de l'URL pour GitHub
      // Assure-toi que repoUrl est au format https://github.com/owner/repo.git
      String[] urlParts = repoUrl.split("/");
      repoOwner = urlParts[urlParts.length - 2];
      repoName = urlParts[urlParts.length - 1].replace(".git", "");
      System.out.println("GitHub Owner: " + repoOwner + ", Repo Name: " + repoName);
    } else if ("gitlab".equalsIgnoreCase(platform)) {
      apiBaseUrl = "https://gitlab.com/api/v4";
      apiToken = "glpat-VOTRE_PAT_GITLAB_POUR_API";
      gitlabProjectId = "12345678"; // <--- REMPLACEZ PAR L'ID NUMÉRIQUE DE VOTRE PROJET GITLAB
      repoOwner = null; // Non utilisé pour GitLab dans cette approche
      repoName = null; // Non utilisé pour GitLab dans cette approche
    } else {
      System.err.println("Unsupported platform: " + platform + ". Please choose 'github' or 'gitlab'.");
      System.exit(1);
      return; // Pour éviter une erreur de compilation sur les variables non initialisées
    }

    String branchName = "rewrite-changes-" + System.currentTimeMillis();
    String commitMessage = "Refactoring by OpenRewrite: " + recipeName;
    String prMrTitle = commitMessage;
    String prMrDescription = "Automated refactoring applied using OpenRewrite recipe: " + recipeName + "\n\n";

    System.out.println("Starting Rewrite operation...");
    System.out.println("Repository URL: " + repoUrl);
    System.out.println("Local path: " + localRepoDir.toAbsolutePath());
    System.out.println("Recipe: " + recipeName);
    System.out.println("Platform: " + platform);

    Git git = null;
    try {
      // 1. Clonage ou mise à jour du dépôt Git
      if (Files.exists(localRepoDir)) {
        System.out.println("Local repository directory already exists. Attempting to delete...");
        try (Stream<Path> walk = Files.walk(localRepoDir)) {
          walk.sorted(java.util.Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        if (Files.exists(localRepoDir)) {
          System.err.println("Failed to delete existing directory. Please ensure it's not in use and permissions are correct.");
          System.exit(1);
        }
      }

      System.out.println("Cloning repository...");
      git = Git.cloneRepository()
        .setURI(repoUrl)
        .setDirectory(localRepoDir.toFile())
        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUsername, gitPat))
        .call();
      System.out.println("Repository cloned successfully to " + localRepoDir.toAbsolutePath());

      Repository repository = git.getRepository();
      // JGit 6.x: repository.getBranch() est déprécié. Utilisez BranchConfig.resolveBranch(repository).
      // Mais pour la simplicité, on peut obtenir le nom de la branche HEAD via RevWalk:
      String defaultBranch;
      try (RevWalk revWalk = new RevWalk(repository)) {
        RevCommit headCommit = revWalk.parseCommit(repository.resolve(Constants.HEAD));
        defaultBranch = headCommit.getParents().length > 0 ? repository.getBranch() : "main"; // Fallback pour les nouveaux dépôts vides
      }
      System.out.println("Default branch detected: " + defaultBranch);

      // 2. Obtenir le classpath du projet cloné (Méthode Maven)
      List<Path> classpath = new ArrayList<>();
      ProcessBuilder processBuilder = new ProcessBuilder(
        "/home/cedric/.sdkman/candidates/maven/current/bin/mvn",
        "dependency:build-classpath",
        "-Dmdep.outputFile=classpath.txt"
      );
      processBuilder.directory(localRepoDir.toFile());
      processBuilder.redirectErrorStream(true);

      System.out.println("Building classpath using Maven...");
      Process process = processBuilder.start();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println(line);
        }
      }

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        System.err.println("Maven classpath build failed with exit code: " + exitCode);
        System.err.println("Please ensure Maven is installed and the project is a valid Maven project.");
      }

      Path classpathFile = localRepoDir.resolve("classpath.txt");
      if (Files.exists(classpathFile)) {
        String cpContent = Files.readString(classpathFile);
        for (String path : cpContent.split(File.pathSeparator)) {
          Path p = Paths.get(path);
          if (Files.exists(p)) {
            classpath.add(p);
          } else {
            System.err.println(
              "Classpath entry not found: " + p + ". This might indicate an issue with Maven's classpath generation or missing artifacts."
            );
          }
        }
        Files.delete(classpathFile);
      } else {
        System.err.println("classpath.txt not found. Maven may not have generated it or there was an issue.");
      }

      System.out.println("Classpath entries found: " + classpath.size());

      // 3. Exécution d'OpenRewrite
      ExecutionContext ctx = new InMemoryExecutionContext();

      Environment env = Environment.builder().scanRuntimeClasspath().build();
      Recipe recipe = env.activateRecipes(recipeName);

      if (recipe == null) {
        System.err.println("Recipe not found: " + recipeName);
        System.exit(1);
      }

      List<Path> sourcePaths;
      try (Stream<Path> walk = Files.walk(localRepoDir)) {
        sourcePaths = walk
          .filter(Files::isRegularFile)
          .filter(
            p ->
              !p.startsWith(localRepoDir.resolve(".git")) &&
              !p.startsWith(localRepoDir.resolve("target")) &&
              !p.startsWith(localRepoDir.resolve("build")) &&
              !p.getFileName().toString().equals("classpath.txt")
          )
          .collect(Collectors.toList());
      }

      System.out.println("Parsing sources...");

      // Correction: Créer une liste pour collecter tous les SourceFile des différents streams
      List<SourceFile> allSourceFiles = new ArrayList<>();

      // Chaque parser retourne un Stream<SourceFile>. Collectez-les dans une liste temporaire, puis ajoutez-les.
      // Ou mieux encore, utilisez Stream.concat ou Stream.flatMap pour les fusionner directement.

      // Option 1: Collecter et ajouter (plus lisible pour des appels séparés)
      allSourceFiles.addAll(JavaParser.fromJavaVersion().classpath(classpath).build().parse(sourcePaths, localRepoDir, ctx).toList()); // Collecte le Stream en List

      //      allSourceFiles.addAll(MavenParser.builder()
      //        .build()
      //        .parse(sourcePaths, localRepoDir, ctx)
      //        .toList()); // Collecte le Stream en List

      allSourceFiles.addAll(XmlParser.builder().build().parse(sourcePaths, localRepoDir, ctx).toList()); // Collecte le Stream en List

      allSourceFiles.addAll(YamlParser.builder().build().parse(sourcePaths, localRepoDir, ctx).toList()); // Collecte le Stream en List

      // La variable 'sourceFiles' doit maintenant être de type List<SourceFile> (ou Collection<SourceFile>)
      // Elle sera ensuite passée au LargeSourceSet.Builder
      //      Collection<SourceFile> sourceFiles = allSourceFiles; // Ou directement List<SourceFile> sourceFiles = allSourceFiles;

      LargeSourceSet largeSourceSet = new InMemoryLargeSourceSet(allSourceFiles);
      System.out.println("Executing recipe " + recipeName + "...");
      RecipeRun recipeRun = recipe.run(largeSourceSet, ctx);
      List<Result> fixedSources = recipeRun.getChangeset().getAllResults();

      // 4. Application des changements et opérations Git
      if (!fixedSources.isEmpty()) {
        System.out.println("Changes found! Applying changes to files...");
        for (Result fixedResult : fixedSources) {
          SourceFile fixedSource = fixedResult.getAfter();
          if (
            (fixedSource.getMarkers() != null && fixedSource.getMarkers().findFirst(org.openrewrite.marker.Generated.class).isPresent()) ||
            fixedSource.getMarkers().findFirst(org.openrewrite.marker.GitProvenance.class).isPresent()
          ) {
            continue;
          }

          Path originalPath = localRepoDir.resolve(fixedSource.getSourcePath());
          Files.createDirectories(originalPath.getParent());
          Files.writeString(originalPath, fixedSource.printAll());
          System.out.println("Modified: " + originalPath);
        }

        // Création d'une nouvelle branche, commit et push
        System.out.println("\nCreating new branch: " + branchName);
        git.branchCreate().setName(branchName).call();
        git.checkout().setName(branchName).call();

        System.out.println("Adding all changes to Git index...");
        git.add().addFilepattern(".").call();

        System.out.println("Committing changes...");
        RevCommit commit = git.commit().setMessage(commitMessage).call();
        System.out.println("Changes committed with ID: " + commit.getId().getName());
        //        System.out.println("Pushing new branch to remote...");
        //        git.push()
        //          .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUsername, gitPat))
        //          .add(branchName)
        //          .call();
        //        System.out.println("Branch " + branchName + " pushed successfully.");

        //        // 5. Création de la Pull Request (GitHub) ou Merge Request (GitLab)
        //        HttpClient client = HttpClient.newHttpClient();
        //        HttpRequest request;
        //        String requestBody;
        //        String endpointUrl;
        //
        //        if ("github".equalsIgnoreCase(platform)) {
        //          // API GitHub pour créer une Pull Request
        //          // POST /repos/{owner}/{repo}/pulls
        //          endpointUrl = String.format("%s/repos/%s/%s/pulls", apiBaseUrl, repoOwner, repoName);
        //          requestBody = String.format("{\"head\":\"%s\",\"base\":\"%s\",\"title\":\"%s\",\"body\":\"%s\"}",
        //            branchName, defaultBranch, prMrTitle, prMrDescription + "Commit: " + commit.getId().getName()
        //          );
        //          request = HttpRequest.newBuilder()
        //            .uri(URI.create(endpointUrl))
        //            .header("Authorization", "token " + apiToken) // GitHub utilise "token"
        //            .header("Accept", "application/vnd.github.v3+json") // Recommandé par GitHub
        //            .header("Content-Type", "application/json")
        //            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        //            .build();
        //          System.out.println("Creating GitHub Pull Request...");
        //
        //        } else { // Assume "gitlab"
        //          // API GitLab pour créer une Merge Request
        //          // POST /projects/:id/merge_requests
        //          endpointUrl = String.format("%s/projects/%s/merge_requests", apiBaseUrl, gitlabProjectId);
        //          requestBody = String.format("{\"source_branch\":\"%s\",\"target_branch\":\"%s\",\"title\":\"%s\",\"description\":\"%s\",\"remove_source_branch\":%b}",
        //            branchName, defaultBranch, prMrTitle, prMrDescription + "Commit: " + commit.getId().getName(),
        //            true // Supprimer la branche source après merge
        //          );
        //          request = HttpRequest.newBuilder()
        //            .uri(URI.create(endpointUrl))
        //            .header("Private-Token", apiToken) // GitLab utilise "Private-Token"
        //            .header("Content-Type", "application/json")
        //            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        //            .build();
        //          System.out.println("Creating GitLab Merge Request...");
        //        }
        //
        //        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //
        //        if (response.statusCode() >= 200 && response.statusCode() < 300) {
        //          System.out.println("Request created successfully!");
        //          System.out.println("Response: " + response.body());
        //        } else {
        //          System.err.println("Failed to create Pull/Merge Request. Status: " + response.statusCode());
        //          System.err.println("Response body: " + response.body());
        //        }

      } else {
        System.out.println("No changes were made by the recipe. No branch created or Pull/Merge Request submitted.");
      }
    } catch (GitAPIException | IOException | InterruptedException e) {
      System.err.println("An error occurred: " + e.getMessage());
      e.printStackTrace();
    } finally {
      if (git != null) {
        git.close();
      }
    }
  }

  // Helper method for JGit TreeIterator - kept for reference, not directly used in PR/MR creation logic
  private static AbstractTreeIterator prepareTreeParser(Repository repository, ObjectId objectId) throws IOException {
    try (RevWalk walk = new RevWalk(repository)) {
      RevCommit commit = walk.parseCommit(objectId);
      RevTree tree = walk.parseTree(commit.getTree().getId());

      CanonicalTreeParser treeParser = new CanonicalTreeParser();
      try (ObjectReader reader = repository.newObjectReader()) {
        treeParser.reset(reader, tree.getId());
      }

      walk.dispose();
      return treeParser;
    }
  }
}
