package fr.rewrite.server.domain.repository;

import fr.rewrite.server.domain.RewriteId;

public interface RepositoryPort {
  void cloneRepository(RepositoryURL repositoryURL);
  void cloneRepository(RepositoryURL repositoryURL, Credentials credential);

  void createBranch(RewriteId rewriteId, String branchName);
}
