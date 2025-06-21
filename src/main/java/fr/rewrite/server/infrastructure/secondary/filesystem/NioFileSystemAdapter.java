package fr.rewrite.server.infrastructure.secondary.filesystem;

import fr.rewrite.server.domain.exception.FileSystemOperationException;
import fr.rewrite.server.domain.spi.DatastorePort;
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

  private static final Logger log = LoggerFactory.getLogger(NioFileSystemAdapter.class);

  @Override
  public void createDatastore(Path path) throws FileSystemOperationException { // Changement
    log.debug("Creating datastore {}", path);

    try {
      Files.createDirectories(path);
      log.info("Directory created: {}", path);
    } catch (IOException e) {
      throw new FileSystemOperationException("Failed to create datastore: " + path + ". " + e.getMessage(), e);
    }
  }

  @Override
  public void deleteDatastore(Path directory) throws FileSystemOperationException { // Changement
    if (!Files.exists(directory)) {
      log.warn("Datastore does not exist, skipping deletion: {}", directory);
      return;
    }
    try {
      Files.walkFileTree(
        directory,
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
      log.info("Datastore deleted: {}", directory);
    } catch (IOException e) {
      throw new FileSystemOperationException("Failed to delete Datastore: " + directory + ". " + e.getMessage(), e);
    }
  }

  @Override
  public Set<Path> listAllFiles(Path directory) throws FileSystemOperationException { // Changement
    if (!Files.isDirectory(directory)) {
      throw new FileSystemOperationException("Path must be a directory: " + directory); // Ou IllegalArgumentException si c'est une validation de paramètre
    }
    try (Stream<Path> walk = Files.walk(directory)) {
      return walk.filter(Files::isRegularFile).collect(Collectors.toSet());
    } catch (IOException e) {
      throw new FileSystemOperationException("Failed to list files in datastore: " + directory + ". " + e.getMessage(), e);
    }
  }
}
