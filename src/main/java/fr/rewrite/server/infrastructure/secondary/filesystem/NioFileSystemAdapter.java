package fr.rewrite.server.infrastructure.secondary.filesystem;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.domain.datastore.DatastoreNotFoundException;
import fr.rewrite.server.domain.datastore.DatastorePort;
import fr.rewrite.server.domain.exception.FileSystemOperationException;
import fr.rewrite.server.domain.state.RewriteConfig;
import fr.rewrite.server.shared.error.domain.Assert;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NioFileSystemAdapter implements DatastorePort {

  public static final String REWRITE_ID = "rewriteId";
  private static final Logger log = LoggerFactory.getLogger(NioFileSystemAdapter.class);

  private final RewriteConfig rewriteConfig;

  public NioFileSystemAdapter(RewriteConfig rewriteConfig) {
    Assert.notNull("rewriteConfig", rewriteConfig);
    this.rewriteConfig = rewriteConfig;
  }

  private Path getPathFromRewriteId(RewriteId rewriteId) {
    Assert.notNull(REWRITE_ID, rewriteId);
    return rewriteConfig.resolve(rewriteId);
  }

  @Override
  public void createDatastore(RewriteId rewriteId) throws FileSystemOperationException {
    Assert.notNull(REWRITE_ID, rewriteId);
    Path datastorePath = getPathFromRewriteId(rewriteId);
    log.debug("Creating datastore {}", datastorePath);

    try {
      Files.createDirectories(datastorePath);
      log.info("Directory created: {}", datastorePath);
    } catch (IOException e) {
      throw new FileSystemOperationException("Failed to create datastore: " + datastorePath + ". " + e.getMessage(), e);
    }
  }

  @Override
  public void deleteDatastore(RewriteId rewriteId) throws FileSystemOperationException {
    Assert.notNull(REWRITE_ID, rewriteId);
    Path datastorePath = getPathFromRewriteId(rewriteId);
    if (!Files.exists(datastorePath)) {
      log.warn("Datastore does not exist, skipping deletion: {}", datastorePath);
      return;
      //TODO Exception
    }
    try {
      Files.walkFileTree(
        datastorePath,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }
        }
      );
      log.info("Datastore deleted: {}", datastorePath);
    } catch (IOException e) {
      throw new FileSystemOperationException("Failed to delete Datastore: " + datastorePath + ". " + e.getMessage(), e);
    }
  }

  @Override
  public Set<Path> listAllFiles(RewriteId rewriteId) throws FileSystemOperationException { // Changement
    Assert.notNull(REWRITE_ID, rewriteId);
    Path datastorePath = getPathFromRewriteId(rewriteId);
    if (!Files.isDirectory(datastorePath)) {
      throw new FileSystemOperationException("Path must be a directory: " + datastorePath); // Ou IllegalArgumentException si c'est une validation de paramètre
    }
    try (Stream<Path> walk = Files.walk(datastorePath)) {
      return walk.filter(Files::isRegularFile).collect(Collectors.toSet());
    } catch (IOException e) {
      throw new FileSystemOperationException("Failed to list files in datastore: " + datastorePath + ". " + e.getMessage(), e);
    }
  }

  @Override
  public Datastore getDatastore(RewriteId rewriteId) {
    Assert.notNull(REWRITE_ID, rewriteId);
    Path datastorePath = getPathFromRewriteId(rewriteId);
    if (!Files.exists(datastorePath)) {
      throw new DatastoreNotFoundException(rewriteId);
    }
    return Datastore.from(rewriteId, rewriteConfig.resolve(rewriteId), listAllFiles(rewriteId));
  }
}
