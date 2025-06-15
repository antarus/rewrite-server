package fr.rewrite.server.infrastructure.secondary.filesystem;

import fr.rewrite.server.domain.FileSystemPort;
import fr.rewrite.server.domain.exception.FileSystemOperationException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class NioFileSystemAdapter implements FileSystemPort {

  @Override
  public void createDirectory(Path path) throws FileSystemOperationException { // Changement
    try {
      Files.createDirectories(path);
      System.out.println("Directory created: " + path);
    } catch (IOException e) {
      throw new FileSystemOperationException("Failed to create directory: " + path + ". " + e.getMessage(), e);
    }
  }

  @Override
  public void deleteDirectory(Path directory) throws FileSystemOperationException { // Changement
    if (!Files.exists(directory)) {
      System.out.println("Directory does not exist, skipping deletion: " + directory);
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
      System.out.println("Directory deleted: " + directory);
    } catch (IOException e) {
      throw new FileSystemOperationException("Failed to delete directory: " + directory + ". " + e.getMessage(), e);
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
      throw new FileSystemOperationException("Failed to list files in directory: " + directory + ". " + e.getMessage(), e);
    }
  }
}
