package fr.rewrite.server.domain;

import fr.rewrite.server.domain.exception.FileSystemOperationException;
import java.nio.file.Path;
import java.util.Set;

public interface FileSystemPort {
  void createDirectory(Path path) throws FileSystemOperationException; // Changement
  void deleteDirectory(Path directory) throws FileSystemOperationException; // Changement
  Set<Path> listAllFiles(Path directory) throws FileSystemOperationException; // Changement
}
