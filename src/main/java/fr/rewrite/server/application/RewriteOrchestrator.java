package fr.rewrite.server.application;

import static java.util.Objects.requireNonNull;

import fr.rewrite.server.application.dto.Credentials; // Assurez-vous que cette classe existe bien dans votre package DTO
import fr.rewrite.server.application.dto.PlatformConfig; // Assurez-vous que cette classe existe bien dans votre package DTO
import fr.rewrite.server.application.dto.PullRequestDetails;
import fr.rewrite.server.application.dto.RewriteConfig;
import fr.rewrite.server.domain.*;
import fr.rewrite.server.domain.exception.BuildToolException;
import fr.rewrite.server.domain.exception.FileSystemOperationException;
import fr.rewrite.server.domain.exception.GitOperationException;
import java.io.IOException;
import java.net.URI; // Ajout de l'import
import java.net.URISyntaxException; // Ajout de l'import
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils; // Importez Apache Commons IO
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Result;
import org.openrewrite.SourceFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RewriteOrchestrator {

  private final GitRepositoryPort gitRepositoryPort;
  private final BuildToolPort buildToolPort;
  private final RewriteEnginePort rewriteEnginePort;
  private final FileSystemPort fileSystemPort;
  private final PullRequestServicePort pullRequestServicePort;

  @Value("${rewrite.server.work-directory}") // Injecte la valeur de application.properties
  private String baseWorkDirectory;

  public RewriteOrchestrator(
    GitRepositoryPort gitRepositoryPort,
    BuildToolPort buildToolPort,
    RewriteEnginePort rewriteEnginePort,
    FileSystemPort fileSystemPort,
    PullRequestServicePort pullRequestServicePort
  ) {
    this.gitRepositoryPort = requireNonNull(gitRepositoryPort, "GitRepositoryPort must not be null");
    this.buildToolPort = requireNonNull(buildToolPort, "BuildToolPort must not be null");
    this.rewriteEnginePort = requireNonNull(rewriteEnginePort, "RewriteEnginePort must not be null");
    this.fileSystemPort = requireNonNull(fileSystemPort, "FileSystemPort must not be null");
    this.pullRequestServicePort = requireNonNull(pullRequestServicePort, "PullRequestServicePort must not be null");
  }

  public void runRewriteProcess(RewriteConfig config) throws GitOperationException, BuildToolException, FileSystemOperationException {
    requireNonNull(config.repoUrl(), "Repository URL must not be null");
    requireNonNull(config.recipeName(), "Recipe name must not be null");
    requireNonNull(config.platform(), "Platform must not be null");
    requireNonNull(config.gitUsername(), "Git username must not be null");
    requireNonNull(config.gitPatForGit(), "Git PAT for Git must not be null");
    requireNonNull(config.gitPatForApi(), "Git PAT for API must not be null");

    // Génération d'un répertoire de travail unique pour cette exécution sur le serveur
    Path workDir = Paths.get(baseWorkDirectory, UUID.randomUUID().toString());

    try {
      // Création du répertoire de base s'il n'existe pas
      fileSystemPort.createDirectory(Paths.get(baseWorkDirectory));
      // Création du répertoire de travail unique
      fileSystemPort.createDirectory(workDir);

      System.out.println("Processing repository in server work directory: " + workDir);

      // 2. Cloner le dépôt
      System.out.println("Cloning repository " + config.repoUrl() + " into " + workDir);
      gitRepositoryPort.cloneRepository(config.repoUrl(), workDir, new Credentials(config.gitUsername(), config.gitPatForGit()));

      // 3. Créer et checkout la nouvelle branche
      String branchName = "rewrite-changes-" + UUID.randomUUID().toString().substring(0, 8); // Générer un nom de branche unique
      System.out.println("Creating new branch: " + branchName);
      gitRepositoryPort.createAndCheckoutBranch(workDir, branchName);

      // 4. Exécuter le build tool (Maven)
      System.out.println("Executing build tool to clean and compile the project...");
      buildToolPort.executeBuild(workDir, config.mavenExecutablePath());

      // 5. Récupérer les chemins des sources et le classpath
      Set<Path> sourceFiles = fileSystemPort.listAllFiles(workDir);
      List<Path> sourcePaths = sourceFiles
        .stream()
        .filter(p -> config.sourceExcludePatterns().stream().noneMatch(p.toString()::contains))
        .collect(Collectors.toList());

      System.out.println("Source paths to parse: " + sourcePaths.size() + " files.");

      // Obtenir le classpath généré par Maven
      Path classpathFile = workDir.resolve("classpath.txt");
      Set<Path> classpath = buildToolPort.getProjectClasspath(workDir, classpathFile, config.mavenExecutablePath());
      System.out.println("Classpath entries: " + classpath.size());

      // 6. Parsing des sources avec OpenRewrite
      System.out.println("Parsing sources with OpenRewrite...");
      ExecutionContext executionContext = new InMemoryExecutionContext();

      List<SourceFile> parsedSources = rewriteEnginePort.parseSources(
        sourcePaths,
        workDir, // baseDir est maintenant le workDir généré sur le serveur
        classpath,
        executionContext
      );
      System.out.println("Parsed " + parsedSources.size() + " source files.");

      // 7. Exécuter la recette de réécriture
      System.out.println("Running OpenRewrite recipe: " + config.recipeName());
      List<Result> results = rewriteEnginePort.runRecipes(parsedSources, config.recipeName(), executionContext);
      System.out.println("Recipe execution completed.");

      //      long changesCount = results.stream().filter(Result::isChange).count();
      System.out.println("Found " + results.size() + " changes.");

      if (!results.isEmpty()) {
        System.out.println("Applying changes to files...");
        // Boucle traditionnelle pour permettre de jeter des exceptions
        for (Result result : results) {
          Path originalRelativePath = result.getAfter().getSourcePath(); // Chemin relatif de l'original SourceFile
          Path absolutePath = workDir.resolve(originalRelativePath); // Chemin absolu dans le workDir

          try {
            if (result.getAfter() == null) {
              // Le fichier a été supprimé
              System.out.println("Deleting file: " + absolutePath);
              Files.deleteIfExists(absolutePath);
            } else if (result.getBefore() == null || result.getAfter() != null) {
              // Le fichier a été ajouté ou modifié (si getBefore est null, c'est un ajout)
              System.out.println("Applying changes to: " + absolutePath);
              Files.createDirectories(absolutePath.getParent()); // Assurez-vous que les répertoires parents existent
              Files.writeString(absolutePath, result.getAfter().printAll());
            }
          } catch (IOException e) {
            System.err.println("Failed to write/delete changes for " + originalRelativePath + ": " + e.getMessage());
            // Il est préférable de lancer une exception ici pour arrêter le processus si l'application des changements échoue
            throw new FileSystemOperationException("Failed to apply changes for file: " + originalRelativePath, e);
          }
        }

        if (config.pushAndPr()) {
          System.out.println("Committing changes...");
          gitRepositoryPort.commitChanges(workDir, config.commitMessage());

          System.out.println("Pushing new branch to remote...");
          gitRepositoryPort.pushBranch(workDir, branchName, new Credentials(config.gitUsername(), config.gitPatForGit()));

          System.out.println("Creating Pull Request / Merge Request...");
          PullRequestDetails prDetails = new PullRequestDetails(
            branchName,
            config.baseBranch(),
            config.prMrTitle(),
            config.prMrDescription(),
            gitRepositoryPort.getLatestCommitId(workDir)
          );

          // --- Extraction du propriétaire et du nom du dépôt de l'URL ---
          String repoOwner = null;
          String repoName = null;
          String platformApiBaseUrl = null;

          try {
            URI uri = new URI(config.repoUrl());
            String path = uri.getPath(); // Ex: "/owner/repo.git" ou "/group/subgroup/repo.git"
            if (path.endsWith(".git")) {
              path = path.substring(0, path.length() - 4); // Supprimer ".git"
            }
            String[] parts = path.split("/");
            // La logique suivante suppose un chemin comme /owner/repo ou /group/repo
            // Pour des chemins plus complexes (ex: /group/subgroup/repo), cela prendra le dernier segment comme repo et l'avant-dernier comme "owner"
            if (parts.length >= 3) { // Minimum / /owner /repo
              repoOwner = parts[parts.length - 2];
              repoName = parts[parts.length - 1];
            } else {
              System.err.println(
                "Warning: Could not parse repo owner and name from URL: " + config.repoUrl() + ". PR/MR creation might fail."
              );
            }

            // --- Détermination de l'URL de base de l'API de la plateforme ---
            if ("github".equalsIgnoreCase(config.platform())) {
              platformApiBaseUrl = "https://api.github.com";
            } else if ("gitlab".equalsIgnoreCase(config.platform())) {
              // Pour les instances GitLab auto-hébergées, cette URL pourrait devoir être configurable
              platformApiBaseUrl = uri.getScheme() + "://" + uri.getHost() + "/api/v4";
            }
            // Ajoutez ici d'autres plateformes si nécessaire (ex: Bitbucket, Azure DevOps)

          } catch (URISyntaxException e) {
            throw new GitOperationException("Invalid repository URL syntax for PR/MR creation: " + config.repoUrl(), e);
          }

          // --- Construction de l'objet PlatformConfig pour le service de PR ---
          PlatformConfig serverPlatformConfig = new PlatformConfig(
            platformApiBaseUrl,
            config.gitPatForApi(), // Le token API
            repoOwner,
            repoName,
            null, // gitlabProjectId - à configurer si nécessaire pour GitLab via une autre source
            config.platform()
          );
          pullRequestServicePort.createPullRequest(prDetails, serverPlatformConfig);
          System.out.println("Pull Request / Merge Request created successfully.");
        } else {
          System.out.println("Skipping push and PR/MR creation as requested.");
        }
      } else {
        System.out.println("No changes detected. Skipping commit and PR/MR creation.");
      }
      System.out.println("Rewrite process completed.");
    } finally {
      // Nettoyage du répertoire de travail à la fin (même en cas d'erreur)
      // a deplacer pour gerer les traitement en plusieur fois
      if (Files.exists(workDir)) {
        try {
          FileUtils.deleteDirectory(workDir.toFile()); // Utilise Apache Commons IO pour la suppression récursive
          System.out.println("Cleaned up server work directory: " + workDir);
        } catch (IOException e) {
          System.err.println("Failed to clean up server work directory " + workDir + ": " + e.getMessage());
        }
      }
    }
  }
}
