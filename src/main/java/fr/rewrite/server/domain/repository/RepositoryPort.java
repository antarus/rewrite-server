package fr.rewrite.server.domain.repository;

public interface RepositoryPort {
  void cloneRepository(RepositoryURL repositoryURL);
  void cloneRepository(RepositoryURL repositoryURL, Credentials credential);
}
