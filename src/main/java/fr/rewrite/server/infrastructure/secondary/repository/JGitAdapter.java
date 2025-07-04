package fr.rewrite.server.infrastructure.secondary.repository;

import fr.rewrite.server.domain.RewriteConfig;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.datastore.DatastorePort;
import fr.rewrite.server.domain.log.CleanSensitiveLog;
import fr.rewrite.server.domain.log.LogPublisher;
import fr.rewrite.server.domain.repository.*;
import fr.rewrite.server.shared.error.domain.Assert;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JGitAdapter implements RepositoryPort {

  public static final String REPOSITORY_CLONED_SUCCESSFULLY = "Repository cloned successfully.";
  public static final String CLONING_INTO = "Cloning %s  into %s ";
  private static final Logger log = LoggerFactory.getLogger(JGitAdapter.class);
  private final RewriteConfig rewriteConfig;
  private final DatastorePort datastorePort;
  private final LogPublisher logPublisher;

  public JGitAdapter(RewriteConfig rewriteConfig, DatastorePort datastorePort, LogPublisher logPublisher) {
    this.rewriteConfig = rewriteConfig;
    this.datastorePort = datastorePort;
    this.logPublisher = logPublisher;
  }

  @Override
  public void cloneRepository(DatastoreId datastoreId, RepositoryURL repositoryURL, Optional<Credentials> credentials) {
    Assert.notNull("datastoreId", datastoreId);
    Assert.notNull("repositoryURL", repositoryURL);
    Assert.notNull("credentials", credentials);
    try {
      Path pathDsRepository = rewriteConfig.resolveDsProject(datastoreId);

      CloneCommand cloneCommand = getGitCloneCommand(datastoreId, repositoryURL, pathDsRepository);

      credentials.ifPresent(creds -> {
        cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(creds.username(), creds.pat()));
      });

      try (Git git = cloneCommand.call()) {
        logPublisher.info(REPOSITORY_CLONED_SUCCESSFULLY, datastoreId);
      }
    } catch (GitAPIException e) {
      throw new CloneRepositoryException(repositoryURL, e);
    }
  }

  @Override
  public void deleteRepository(DatastoreId datastoreId) {
    datastorePort.deleteDatastore(datastoreId);
  }

  @Override
  public void createBranchAndCheckout(DatastoreId datastoreId, RepositoryBranchName repositoryBranchName) throws CreateBranchException {
    Assert.notNull("datastoreId", datastoreId);
    Assert.notNull("repositoryBranchName", repositoryBranchName);

    Path pathDsRepository = rewriteConfig.resolveDsProject(datastoreId);
    try (Git git = Git.open(pathDsRepository.toFile())) {
      git.checkout().setCreateBranch(true).setName(repositoryBranchName.name()).call();
      logPublisher.info(String.format("Branch %s created in repository %s", repositoryBranchName.name(), datastoreId.get()), datastoreId);
    } catch (IOException | GitAPIException e) {
      throw new CreateBranchException(datastoreId, repositoryBranchName.name(), e);
    }
  }

  private CloneCommand getGitCloneCommand(DatastoreId datastoreId, RepositoryURL repositoryURL, Path pathDatastore) {
    logPublisher.info(String.format(CLONING_INTO, repositoryURL.url(), pathDatastore), datastoreId);

    return Git.cloneRepository().setURI(repositoryURL.url()).setDirectory(pathDatastore.toFile());
  }
}
