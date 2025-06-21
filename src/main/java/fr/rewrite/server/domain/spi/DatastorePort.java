package fr.rewrite.server.domain.spi;

import fr.rewrite.server.domain.exception.FileSystemOperationException;
import java.nio.file.Path;
import java.util.Set;

public interface DatastorePort {
  void createDatastore(Path path) throws FileSystemOperationException; // Changement
  void deleteDatastore(Path directory) throws FileSystemOperationException; // Changement
  Set<Path> listAllFiles(Path directory) throws FileSystemOperationException; // Changement
}
