package fr.rewrite.server.domain.repository;

import fr.rewrite.server.domain.RewriteId;

public interface RepositoryManager {
  void cloneRepository(RepositoryURL repositoryURL, Credentials credential);

  void createBranch(RewriteId rewriteId, String currentBranch);
}
