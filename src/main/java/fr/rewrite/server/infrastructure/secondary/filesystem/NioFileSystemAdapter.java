package fr.rewrite.server.infrastructure.secondary.filesystem;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.rewrite.server.domain.RewriteConfig;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.datastore.DatastoreOperationException;
import fr.rewrite.server.domain.datastore.DatastorePort;
import fr.rewrite.server.domain.datastore.DatastoreSavable;
import fr.rewrite.server.domain.log.CleanSensitiveLog;
import fr.rewrite.server.domain.log.LogPublisher;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class NioFileSystemAdapter implements DatastorePort {

  public static final String REWRITE_ID = "rewriteId";
  public static final String DATASTORE_ID = "datastoreId";
  private static final Logger log = LoggerFactory.getLogger(NioFileSystemAdapter.class);
  private final LogPublisher logPublisher;
  private final RewriteConfig rewriteConfig;
  private final ObjectMapper objectMapper;
  private final CleanSensitiveLog cleanSensitiveLog;

  public NioFileSystemAdapter(
    LogPublisher logPublisher,
    RewriteConfig rewriteConfig,
    @Qualifier("objectMapperDs") ObjectMapper objectMapper,
    CleanSensitiveLog cleanSensitiveLog
  ) {
    Assert.notNull("rewriteConfig", rewriteConfig);
    this.logPublisher = logPublisher;
    this.objectMapper = objectMapper;
    this.cleanSensitiveLog = cleanSensitiveLog;

    this.rewriteConfig = rewriteConfig;
  }

  private Path getPathDsFromDatastoreId(DatastoreId datastoreId) {
    Assert.notNull(DATASTORE_ID, datastoreId);
    return rewriteConfig.resolveDs(datastoreId);
  }

  @Override
  public void provisionDatastore(DatastoreId datastoreId) throws DatastoreOperationException {
    Assert.notNull(DATASTORE_ID, datastoreId);
    Path datastorePath = getPathDsFromDatastoreId(datastoreId);

    logPublisher.debug("Creating datastore " + datastorePath, datastoreId);

    try {
      Files.createDirectories(datastorePath);

      logPublisher.info("Directory created: " + datastorePath, datastoreId);

      Files.createDirectories(Path.of(datastorePath.toString(), rewriteConfig.dsCache()));
      Files.createDirectories(Path.of(datastorePath.toString(), rewriteConfig.dsRepository()));
    } catch (IOException e) {
      throw new DatastoreOperationException("Failed to create datastore: " + datastorePath + ". ", e);
    }
  }

  @Override
  public void deleteDatastore(DatastoreId datastoreId) {
    Assert.notNull(DATASTORE_ID, datastoreId);
    Path datastorePath = getPathDsFromDatastoreId(datastoreId);
    if (!Files.exists(datastorePath)) {
      logPublisher.warn("Datastore does not exist, skipping deletion: " + datastorePath, datastoreId);
      return;
    }
    try {
      deleteRecursively(datastorePath);
      logPublisher.info("Datastore deleted: " + datastorePath, datastoreId);
    } catch (IOException e) {
      throw new DatastoreOperationException("Failed to delete Datastore: " + datastorePath + ". " + e.getMessage(), e);
    }
  }

  @Override
  public void deleteRepository(DatastoreId datastoreId) {
    Assert.notNull(DATASTORE_ID, datastoreId);
    Path datastorePath = getPathDsFromDatastoreId(datastoreId);
    Path repositoryPath = Path.of(datastorePath.toString(), rewriteConfig.dsRepository());
    if (!Files.exists(repositoryPath)) {
      if (log.isWarnEnabled()) {
        logPublisher.warn(
          "Repository does not exist in datastore " + datastorePath + " , skipping deletion repository : " + repositoryPath,
          datastoreId
        );
      }
      return;
    }
    try {
      deleteRecursively(repositoryPath);
      log.info("Repository deleted: {}", repositoryPath);
    } catch (IOException e) {
      throw new DatastoreOperationException(
        "Failed to delete Repository: " + repositoryPath + " in Datastore " + datastorePath + ". " + e.getMessage(),
        e
      );
    }
  }

  private void deleteRecursively(Path repositoryPath) throws IOException {
    Files.walkFileTree(
      repositoryPath,
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
  }

  @Override
  public Set<Path> listAllFiles(DatastoreId datastoreId) {
    Assert.notNull(DATASTORE_ID, datastoreId);
    Path datastorePath = getPathDsFromDatastoreId(datastoreId);
    if (!Files.isDirectory(datastorePath)) {
      throw new DatastoreOperationException("Path must be a directory: " + datastorePath);
    }
    try (Stream<Path> walk = Files.walk(datastorePath)) {
      return walk.filter(Files::isRegularFile).collect(Collectors.toSet());
    } catch (IOException e) {
      throw new DatastoreOperationException(
        cleanSensitiveLog.clean("Failed to list files in datastore: " + datastorePath + ". " + e.getMessage()),
        e
      );
    }
  }

  @Override
  public boolean exists(DatastoreId id) {
    Path datastorePath = getPathDsFromDatastoreId(id);
    return Files.exists(datastorePath);
  }

  @Override
  public boolean saveObjectToDsCache(DatastoreId dsId, String filename, DatastoreSavable object) {
    Assert.notNull("filename", filename);
    Assert.notNull("object", object);

    Path dsPath = rewriteConfig.resolveDsCache(dsId);

    try {
      Path filePath = dsPath.resolve(filename);
      objectMapper.writeValue(filePath.toFile(), object);

      logPublisher.debug(String.format("Save Object in file '%s'", filePath), dsId);

      return true;
    } catch (Exception e) {
      throw new DatastoreOperationException(e.getMessage(), e);
    }
  }

  @Override
  public DatastoreSavable getObjectToDsCache(DatastoreId dsId, String filename) {
    Assert.notNull("filename", filename);

    Path dsPath = rewriteConfig.resolveDsCache(dsId);

    try {
      Path filePath = dsPath.resolve(filename);

      JsonFactory jfactory = new JsonFactory();
      JsonParser jParser = jfactory.createParser(filePath.toFile());

      DatastoreSavable datastoreSavable = objectMapper.readValue(jParser, DatastoreSavable.class);

      logPublisher.debug(String.format("Get '%s' from file '%s'", datastoreSavable, dsPath), dsId);

      return datastoreSavable;
    } catch (Exception e) {
      throw new DatastoreOperationException(e.getMessage(), e);
    }
  }
}
