package fr.rewrite.server.domain.repository;

public interface RepositoryManager {
  void cloneRepository(RepositoryURL repositoryURL, Credentials credential);
}
