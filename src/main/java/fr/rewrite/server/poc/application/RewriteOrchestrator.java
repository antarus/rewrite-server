package fr.rewrite.server.poc.application;

import static java.util.Objects.requireNonNull;

import fr.rewrite.server.domain.Job;
import fr.rewrite.server.domain.datastore.DatastorePort;
import fr.rewrite.server.domain.exception.BuildToolException;
import fr.rewrite.server.domain.exception.FileSystemOperationException;
import fr.rewrite.server.domain.exception.GitOperationException;
import fr.rewrite.server.domain.repository.RepositoryPort;
import fr.rewrite.server.domain.spi.*;
import fr.rewrite.server.poc.application.dto.RewriteConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RewriteOrchestrator {

  private final RepositoryPort gitRepositoryPort;
  private final BuildToolPort buildToolPort;
  private final RewriteEnginePort rewriteEnginePort;
  private final DatastorePort fileSystemPort;
  private final PullRequestServicePort pullRequestServicePort;

  @Value("${rewrite.server.work}")
  private String baseWorkDirectory;

  // Pas de JobService ici pour le moment. Il sera injecté dans le Tasklet.
  // L'orchestrateur recevra le JobService en paramètre de sa méthode.
  public RewriteOrchestrator(
    RepositoryPort gitRepositoryPort,
    BuildToolPort buildToolPort,
    RewriteEnginePort rewriteEnginePort,
    DatastorePort fileSystemPort,
    PullRequestServicePort pullRequestServicePort
  ) {
    this.gitRepositoryPort = requireNonNull(gitRepositoryPort, "GitRepositoryPort must not be null");
    this.buildToolPort = requireNonNull(buildToolPort, "BuildToolPort must not be null");
    this.rewriteEnginePort = requireNonNull(rewriteEnginePort, "RewriteEnginePort must not be null");
    this.fileSystemPort = requireNonNull(fileSystemPort, "FileSystemPort must not be null");
    this.pullRequestServicePort = requireNonNull(pullRequestServicePort, "PullRequestServicePort must not be null");
  }

  // --- MODIFIED SIGNATURE pour inclure JobService et jobId ---
  public void runRewriteProcess(String jobId, RewriteConfig config, JobService jobService)
    throws GitOperationException, BuildToolException, FileSystemOperationException {
    // Logique de validation (simplifiée car déjà faite en amont)
    requireNonNull(config.repoUrl(), "Repo URL must not be null");
    requireNonNull(config.recipeName(), "Recipe name must not be null");
    requireNonNull(config.gitUsername(), "Git username must not be null");
    requireNonNull(config.gitPatForGit(), "Git PAT for Git must not be null");
    requireNonNull(config.gitPatForApi(), "Git PAT for API must not be null");

    //    RewriteId id = RewriteId.from(config.repoUrl());

    //create datastore
    //clone repository
    // creation de la branche
    // Recuperer le classpath
    // parser les source avec openrewrite ( essayer de les stocker)
    //execution de la recette
    // afficher les diff
    // dans l'ideal pouvoir modifier
    // commiter et pusher
    // et supprimer si plus de recette a passer
    Path workDir = Paths.get(baseWorkDirectory, "job-" + jobId); // Nom de répertoire spécifique au job

    try {
      //      fileSystemPort.createDatastore(Paths.get(baseWorkDirectory));
      //      fileSystemPort.createDatastore(workDir);
      jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Server work directory created: " + workDir, null, null);
      System.out.println("Processing repository for job " + jobId + " in: " + workDir);

      // 2. Cloner le dépôt
      jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Cloning repository...", null, null);
      System.out.println("Cloning repository " + config.repoUrl() + " into " + workDir);
      //            gitRepositoryPort.cloneRepository(config.repoUrl(), workDir, new Credentials(config.gitUsername(), config.gitPatForGit()));

      // 3. Créer et checkout la nouvelle branche
      String branchName = "rewrite-changes-" + UUID.randomUUID().toString().substring(0, 8);
      jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Creating new branch: " + branchName, null, null);
      System.out.println("Creating new branch: " + branchName);
      //      gitRepositoryPort.createAndCheckoutBranch(workDir, branchName);

      // 4. Exécuter le build tool (Maven)
      jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Executing build tool...", null, null);
      System.out.println("Executing build tool to clean and compile the project...");
      buildToolPort.executeBuild(workDir, config.mavenExecutablePath());

      // 5. Récupérer les chemins des sources et le classpath
      jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Parsing source paths...", null, null);
      //      Set<Path> sourceFiles = fileSystemPort.listAllFiles(RewriteId.from("dddd"));
      //      List<Path> sourcePaths = sourceFiles
      //        .stream()
      //        .filter(p -> config.sourceExcludePatterns().stream().noneMatch(p.toString()::contains))
      //        .collect(Collectors.toList());
      //      System.out.println("Source paths to parse: " + sourcePaths.size() + " files.");

      Path classpathFile = workDir.resolve("classpath.txt");
      Set<Path> classpath = buildToolPort.getProjectClasspath(workDir, classpathFile, config.mavenExecutablePath());
      System.out.println("Classpath entries: " + classpath.size());

      // 6. Parsing des sources avec OpenRewrite
      jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Parsing sources with OpenRewrite...", null, null);
      System.out.println("Parsing sources with OpenRewrite...");
      ExecutionContext executionContext = new InMemoryExecutionContext();
      //      List<SourceFile> parsedSources = rewriteEnginePort.parseSources(sourcePaths, workDir, classpath, executionContext);
      //      System.out.println("Parsed " + parsedSources.size() + " source files.");
      //
      //      // 7. Exécuter la recette de réécriture
      //      jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Running OpenRewrite recipe: " + config.recipeName(), null, null);
      //      System.out.println("Running OpenRewrite recipe: " + config.recipeName());
      //      List<Result> results = rewriteEnginePort.runRecipes(parsedSources, config.recipeName(), executionContext);
      //      System.out.println("Recipe execution completed.");
      //
      //      long changesCount = results.size(); // results.stream().filter(Result::isChange).count();
      //      jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Found " + changesCount + " changes.", null, null);
      //      System.out.println("Found " + changesCount + " changes.");
      //
      //      if (changesCount > 0) {
      //        jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Applying changes to files...", null, null);
      //        System.out.println("Applying changes to files...");
      //        for (Result result : results) {
      //          Path originalRelativePath;
      //          if (result.getAfter() != null) {
      //            originalRelativePath = result.getAfter().getSourcePath();
      //          } else if (result.getBefore() != null) {
      //            originalRelativePath = result.getBefore().getSourcePath();
      //          } else {
      //            continue;
      //          }
      //
      //          Path absolutePath = workDir.resolve(originalRelativePath);
      //
      //          try {
      //            if (result.getAfter() == null) {
      //              System.out.println("Deleting file: " + absolutePath);
      //              Files.deleteIfExists(absolutePath);
      //            } else if (result.getBefore() == null || result.getAfter() != null) {
      //              System.out.println("Applying changes to: " + absolutePath);
      //              Files.createDirectories(absolutePath.getParent());
      //              Files.writeString(absolutePath, result.getAfter().printAll());
      //            }
      //          } catch (IOException e) {
      //            System.err.println("Failed to write/delete changes for " + originalRelativePath + ": " + e.getMessage());
      //            throw new FileSystemOperationException("Failed to apply changes for file: " + originalRelativePath, e);
      //          }
      //        }
      //
      //        if (config.pushAndPr()) {
      //          jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Committing changes...", null, null);
      //          System.out.println("Committing changes...");
      //          gitRepositoryPort.commitChanges(workDir, config.commitMessage());
      //
      //          jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Pushing new branch to remote...", null, null);
      //          System.out.println("Pushing new branch to remote...");
      ////          gitRepositoryPort.pushBranch(workDir, branchName, new Credentials(config.gitUsername(), config.gitPatForGit()));
      //
      //          jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Creating Pull Request / Merge Request...", null, null);
      //          System.out.println("Creating Pull Request / Merge Request...");
      //          PullRequestDetails prDetails = new PullRequestDetails(
      //            branchName,
      //            config.baseBranch(),
      //            config.prMrTitle(),
      //            config.prMrDescription(),
      //            gitRepositoryPort.getLatestCommitId(workDir)
      //          );
      //
      //          String repoOwner = null;
      //          String repoName = null;
      //          String platformApiBaseUrl = null;
      //
      //          try {
      //            URI uri = new URI(config.repoUrl());
      //            String path = uri.getPath();
      //            if (path.endsWith(".git")) {
      //              path = path.substring(0, path.length() - 4);
      //            }
      //            String[] parts = path.split("/");
      //            if (parts.length >= 3) {
      //              repoOwner = parts[parts.length - 2];
      //              repoName = parts[parts.length - 1];
      //            } else {
      //              System.err.println(
      //                "Warning: Could not parse repo owner and name from URL: " + config.repoUrl() + ". PR/MR creation might fail."
      //              );
      //            }
      //
      //            if ("github".equalsIgnoreCase(config.platform())) {
      //              platformApiBaseUrl = "https://api.github.com";
      //            } else if ("gitlab".equalsIgnoreCase(config.platform())) {
      //              platformApiBaseUrl = uri.getScheme() + "://" + uri.getHost() + "/api/v4";
      //            }
      //          } catch (URISyntaxException e) {
      //            throw new GitOperationException("Invalid repository URL syntax for PR/MR creation: " + config.repoUrl(), e);
      //          }
      //
      //          PlatformConfig serverPlatformConfig = new PlatformConfig(
      //            platformApiBaseUrl,
      //            config.gitPatForApi(),
      //            repoOwner,
      //            repoName,
      //            null,
      //            config.platform()
      //          );
      ////          pullRequestServicePort.createPullRequest(prDetails, serverPlatformConfig);
      //          jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Pull Request / Merge Request created.", null, "URL_DU_PR_ICI"); // TODO: Get actual PR URL
      //          System.out.println("Pull Request / Merge Request created successfully.");
      //        } else {
      //          jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Skipping push and PR/MR creation as requested.", null, null);
      //          System.out.println("Skipping push and PR/MR creation as requested.");
      //        }
      //      } else {
      //        jobService.updateJobStatus(jobId, Job.Status.RUNNING, "No changes detected. Skipping commit and PR/MR creation.", null, null);
      //        System.out.println("No changes detected. Skipping commit and PR/MR creation.");
      //      }
      System.out.println("Rewrite process completed.");
      // Le statut final (COMPLETED/FAILED) sera géré par le Tasklet ou le JobTriggerService
    } finally {
      if (Files.exists(workDir)) {
        try {
          FileUtils.deleteDirectory(workDir.toFile());
          jobService.updateJobStatus(jobId, Job.Status.RUNNING, "Cleaned up server work directory: " + workDir, null, null);
          System.out.println("Cleaned up server work directory: " + workDir);
        } catch (IOException e) {
          System.err.println("Failed to clean up server work directory " + workDir + ": " + e.getMessage());
          // Considérez la mise à jour du statut du job en ÉCHEC ici si le nettoyage est critique
        }
      }
    }
  }
}
