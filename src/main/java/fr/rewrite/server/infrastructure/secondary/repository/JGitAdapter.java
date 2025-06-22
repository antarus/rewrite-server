package fr.rewrite.server.infrastructure.secondary.repository;

import fr.rewrite.server.domain.exception.GitOperationException;
import fr.rewrite.server.domain.repository.Credentials;
import fr.rewrite.server.domain.repository.RepositoryPort;
import fr.rewrite.server.domain.repository.RepositoryURL;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jetbrains.annotations.Nullable;

public class JGitAdapter implements RepositoryPort {

  @Override
  public void cloneRepository(RepositoryURL repositoryURL, @Nullable Credentials credential) {}
}
