package fr.rewrite.server.domain;

import fr.rewrite.server.application.dto.Credentials;
import fr.rewrite.server.domain.exception.GitOperationException;
import java.nio.file.Path;

public interface GitRepositoryPort {
  void cloneRepository(String repoUrl, Path localPath, Credentials credentials) throws GitOperationException;
  void createAndCheckoutBranch(Path repoPath, String branchName) throws GitOperationException;
  void commitChanges(Path repoPath, String commitMessage) throws GitOperationException;
  void pushBranch(Path repoPath, String branchName, Credentials credentials) throws GitOperationException;
  String getLatestCommitId(Path repoPath) throws GitOperationException;
}
