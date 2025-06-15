package fr.rewrite.server.infrastructure.secondary.git;

import fr.rewrite.server.application.dto.Credentials;
import fr.rewrite.server.domain.GitRepositoryPort;
import fr.rewrite.server.domain.exception.GitOperationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

@Component
public class JGitAdapter implements GitRepositoryPort {

  @Override
  public void cloneRepository(String repoUrl, Path localPath, Credentials credentials)
    throws fr.rewrite.server.domain.exception.GitOperationException {
    try {
      System.out.println("Cloning " + repoUrl + " into " + localPath);
      Git.cloneRepository()
        .setURI(repoUrl)
        .setDirectory(localPath.toFile())
        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(credentials.username(), credentials.pat()))
        .call();
      System.out.println("Repository cloned successfully.");
    } catch (GitAPIException e) {
      throw new GitOperationException("Failed to clone repository: " + e.getMessage(), e);
    }
  }

  @Override
  public void createAndCheckoutBranch(Path repoPath, String branchName) throws GitOperationException {
    try (Git git = Git.open(repoPath.toFile())) {
      String fullBranchName = "refs/heads/" + branchName;
      if (git.getRepository().findRef(fullBranchName) != null) {
        // Si la branche existe déjà, on la checkout
        System.out.println("Branch '" + branchName + "' already exists, checking it out.");
        git.checkout().setName(branchName).call();
      } else {
        // Sinon, on la crée et on la checkout
        System.out.println("Creating and checking out new branch: " + branchName);
        git.checkout().setCreateBranch(true).setName(branchName).call();
      }
    } catch (IOException | GitAPIException e) {
      throw new GitOperationException("Failed to create and checkout branch '" + branchName + "': " + e.getMessage(), e);
    }
  }

  @Override
  public void commitChanges(Path repoPath, String commitMessage) throws GitOperationException {
    try (Git git = Git.open(repoPath.toFile())) {
      git.add().addFilepattern(".").call(); // Add all changes
      RevCommit commit = git.commit().setMessage(commitMessage).call();
      System.out.println("Changes committed with ID: " + commit.getId().getName());
    } catch (IOException | GitAPIException e) {
      throw new GitOperationException("Failed to commit changes: " + e.getMessage(), e);
    }
  }

  @Override
  public void pushBranch(Path repoPath, String branchName, Credentials credentials) throws GitOperationException {
    try (Git git = Git.open(repoPath.toFile())) {
      CredentialsProvider cp = new UsernamePasswordCredentialsProvider(credentials.username(), credentials.pat());
      PushCommand pushCommand = git.push();
      pushCommand.setCredentialsProvider(cp);
      pushCommand.add(branchName); // Push the specific branch
      pushCommand.call();
      System.out.println("Branch '" + branchName + "' pushed successfully.");
    } catch (IOException | GitAPIException e) {
      throw new GitOperationException("Failed to push branch '" + branchName + "': " + e.getMessage(), e);
    }
  }

  @Override
  public String getLatestCommitId(Path repoPath) throws GitOperationException {
    try (Repository repository = new FileRepositoryBuilder().setGitDir(new File(repoPath.toFile(), ".git")).build()) {
      ObjectId head = repository.resolve("HEAD");
      if (head == null) {
        throw new GitOperationException("HEAD commit not found in repository: " + repoPath);
      }
      return head.getName(); // Returns the full SHA-1 hash
    } catch (IOException e) {
      throw new GitOperationException("Failed to get latest commit ID for repository: " + repoPath + ": " + e.getMessage(), e);
    }
  }
}
