package fr.rewrite.server.infrastructure.secondary.repository;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.repository.CloneRepositoryException;
import fr.rewrite.server.domain.repository.Credentials;
import fr.rewrite.server.domain.repository.RepositoryPort;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.domain.state.RewriteConfig;
import fr.rewrite.server.shared.error.domain.Assert;
import java.nio.file.Path;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JGitAdapter implements RepositoryPort {

  public static final String REPOSITORY_CLONED_SUCCESSFULLY = "Repository cloned successfully.";
  public static final String CLONING_INTO = "Cloning {}  into {} ";
  private static final Logger log = LoggerFactory.getLogger(JGitAdapter.class);
  private final RewriteConfig rewriteConfig;

  public JGitAdapter(RewriteConfig rewriteConfig) {
    this.rewriteConfig = rewriteConfig;
  }

  @Override
  public void cloneRepository(RepositoryURL repositoryURL) {
    Assert.notNull("repositoryURL", repositoryURL);
    try {
      Path pathDatastore = rewriteConfig.resolve(RewriteId.from(repositoryURL));

      CloneCommand cloneCommand = getGitCloneCommand(repositoryURL, pathDatastore);

      try (Git git = cloneCommand.call()) {
        // We can now work with the Git object to interact with our cloned repository
        log.info(REPOSITORY_CLONED_SUCCESSFULLY);
      }
    } catch (GitAPIException e) {
      throw new CloneRepositoryException(repositoryURL, e);
    }
  }

  @Override
  public void cloneRepository(RepositoryURL repositoryURL, Credentials credential) {
    Assert.notNull("repositoryURL", repositoryURL);
    Assert.notNull("credential", credential);
    try {
      Path pathDatastore = rewriteConfig.resolve(RewriteId.from(repositoryURL));

      CloneCommand cloneCommand = getGitCloneCommand(repositoryURL, pathDatastore).setCredentialsProvider(
        new UsernamePasswordCredentialsProvider(credential.username(), credential.pat())
      );
      try (Git git = cloneCommand.call()) {
        log.info(REPOSITORY_CLONED_SUCCESSFULLY);
      }
    } catch (GitAPIException e) {
      throw new CloneRepositoryException(repositoryURL, e);
    }
  }

  private CloneCommand getGitCloneCommand(RepositoryURL repositoryURL, Path pathDatastore) {
    log.info(CLONING_INTO, repositoryURL.url(), rewriteConfig.maskWorkdirectory(pathDatastore));
    return Git.cloneRepository().setURI(repositoryURL.url()).setDirectory(pathDatastore.toFile());
  }
}
