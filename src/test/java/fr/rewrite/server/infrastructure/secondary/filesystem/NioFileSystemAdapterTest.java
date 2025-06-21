package fr.rewrite.server.infrastructure.secondary.filesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.exception.FileSystemOperationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.slf4j.LoggerFactory;

@UnitTest
class NioFileSystemAdapterTest {

  private NioFileSystemAdapter nioFileSystemAdapter;
  private ListAppender<ILoggingEvent> listAppender;
  private Logger logger;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    logger = loggerContext.getLogger(NioFileSystemAdapter.class.getName());
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    logger.setLevel(Level.DEBUG);

    nioFileSystemAdapter = new NioFileSystemAdapter();
  }

  @AfterEach
  void tearDown() {
    logger.detachAppender(listAppender);
    listAppender.stop();
  }

  @Test
  void createDatastore_shouldCreateDirectory_whenSuccessful() throws FileSystemOperationException, IOException {
    Path newDatastorePath = tempDir.resolve("new_datastore");

    nioFileSystemAdapter.createDatastore(newDatastorePath);

    assertThat(Files.isDirectory(newDatastorePath)).isTrue();

    List<ILoggingEvent> logs = listAppender.list;
    assertThat(logs).hasSize(2);
    assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
    assertThat(logs.get(0).getFormattedMessage()).contains("Creating datastore");
    assertThat(logs.get(1).getLevel()).isEqualTo(Level.INFO);
    assertThat(logs.get(1).getFormattedMessage()).contains("Directory created: " + newDatastorePath);
  }

  @Test
  void createDatastore_shouldThrowException_whenIOExceptionOccurs() {
    Path failingPath = tempDir.resolve("failing_datastore");

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.createDirectories(failingPath)).thenThrow(new IOException("Permission denied"));
      mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);

      assertThatThrownBy(() -> nioFileSystemAdapter.createDatastore(failingPath))
        .isInstanceOf(FileSystemOperationException.class)
        .hasMessageContaining("Failed to create datastore: " + failingPath)
        .hasCauseInstanceOf(IOException.class);

      List<ILoggingEvent> logs = listAppender.list;
      assertThat(logs).hasSize(1);
      assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
      assertThat(logs.get(0).getFormattedMessage()).contains("Creating datastore");
    }
  }

  @Test
  void deleteDatastore_shouldDeleteDirectory_whenExistsAndSuccessful() throws IOException, FileSystemOperationException {
    Path datastoreToDelete = tempDir.resolve("to_delete_datastore");
    Files.createDirectories(datastoreToDelete);
    Files.createFile(datastoreToDelete.resolve("file1.txt"));
    Files.createDirectories(datastoreToDelete.resolve("subdir"));
    Files.createFile(datastoreToDelete.resolve("subdir").resolve("file2.txt"));

    nioFileSystemAdapter.deleteDatastore(datastoreToDelete);
    assertThat(Files.exists(datastoreToDelete)).isFalse();
    List<ILoggingEvent> logs = listAppender.list;

    assertThat(logs).isNotEmpty();
    assertThat(logs.stream().filter(l -> l.getLevel() == Level.INFO).map(ILoggingEvent::getFormattedMessage)).contains(
      "Datastore deleted: " + datastoreToDelete
    );
  }

  @Test
  void deleteDatastore_shouldLogWarn_whenDatastoreDoesNotExist() throws FileSystemOperationException {
    Path nonExistentDatastore = tempDir.resolve("non_existent");

    nioFileSystemAdapter.deleteDatastore(nonExistentDatastore);
    assertThat(Files.exists(nonExistentDatastore)).isFalse();

    List<ILoggingEvent> logs = listAppender.list;
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getLevel()).isEqualTo(Level.WARN);
    assertThat(logs.get(0).getFormattedMessage()).contains("Datastore does not exist, skipping deletion: " + nonExistentDatastore);
  }

  @Test
  void deleteDatastore_shouldThrowException_whenIOExceptionOccursDuringDeletion() {
    Path failingDeleteDir = tempDir.resolve("failing_delete_dir");
    try {
      Files.createDirectories(failingDeleteDir);
      Files.createFile(failingDeleteDir.resolve("file.txt"));
    } catch (IOException e) {
      throw new RuntimeException("Test setup failed", e);
    }

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.exists(failingDeleteDir)).thenReturn(true);
      mockedFiles.when(() -> Files.isDirectory(failingDeleteDir)).thenReturn(true);
      mockedFiles.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);

      mockedFiles
        .when(() -> Files.walkFileTree(eq(failingDeleteDir), any(SimpleFileVisitor.class)))
        .thenThrow(new IOException("Disk full during deletion"));

      assertThatThrownBy(() -> nioFileSystemAdapter.deleteDatastore(failingDeleteDir))
        .isInstanceOf(FileSystemOperationException.class)
        .hasMessageContaining("Failed to delete Datastore: " + failingDeleteDir)
        .hasCauseInstanceOf(IOException.class);

      List<ILoggingEvent> logs = listAppender.list;
      assertThat(logs).isEmpty();
    }
  }

  @Test
  void listAllFiles_shouldReturnAllRegularFilesInDirectory() throws IOException, FileSystemOperationException {
    Path testDir = tempDir.resolve("list_files_test");
    Files.createDirectories(testDir);
    Path file1 = Files.createFile(testDir.resolve("file1.txt"));
    Path file2 = Files.createFile(testDir.resolve("file2.log"));
    Files.createDirectories(testDir.resolve("subdir"));
    Files.createFile(testDir.resolve("subdir").resolve("nested.json"));

    Set<Path> files = nioFileSystemAdapter.listAllFiles(testDir);

    assertThat(files).containsExactlyInAnyOrder(file1, file2, testDir.resolve("subdir").resolve("nested.json"));
    assertThat(listAppender.list).isEmpty();
  }

  @Test
  void listAllFiles_shouldThrowException_whenPathIsNotDirectory() {
    Path filePath = tempDir.resolve("not_a_dir.txt");
    try {
      Files.createFile(filePath);
    } catch (IOException e) {
      throw new RuntimeException("Test setup failed", e);
    }

    assertThatThrownBy(() -> nioFileSystemAdapter.listAllFiles(filePath))
      .isInstanceOf(FileSystemOperationException.class)
      .hasMessageContaining("Path must be a directory: " + filePath);

    assertThat(listAppender.list).isEmpty();
  }

  @Test
  void listAllFiles_shouldThrowException_whenIOExceptionOccursDuringWalk() {
    Path failingListDir = tempDir.resolve("failing_list_dir");
    try {
      Files.createDirectories(failingListDir);
    } catch (IOException e) {
      throw new RuntimeException("Test setup failed", e);
    }

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.isDirectory(failingListDir)).thenReturn(true);
      mockedFiles.when(() -> Files.walk(failingListDir)).thenThrow(new IOException("Read error"));

      assertThatThrownBy(() -> nioFileSystemAdapter.listAllFiles(failingListDir))
        .isInstanceOf(FileSystemOperationException.class)
        .hasMessageContaining("Failed to list files in datastore: " + failingListDir)
        .hasCauseInstanceOf(IOException.class);

      assertThat(listAppender.list).isEmpty();
    }
  }
}
