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
import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.domain.datastore.DatastoreNotFoundException;
import fr.rewrite.server.domain.exception.FileSystemOperationException;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.domain.state.RewriteConfig;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
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
  Path tempConfigDirectory;

  @TempDir
  Path tempWorkDirectory;

  private RewriteConfig rewriteConfig;

  @BeforeEach
  void setUp() {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    logger = loggerContext.getLogger(NioFileSystemAdapter.class.getName());
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    logger.setLevel(Level.DEBUG);
    rewriteConfig = new RewriteConfig(tempConfigDirectory, tempWorkDirectory);
    nioFileSystemAdapter = new NioFileSystemAdapter(rewriteConfig);
  }

  @AfterEach
  void tearDown() {
    logger.detachAppender(listAppender);
    listAppender.stop();
  }

  @Test
  void createDatastore_shouldCreateDirectory_whenSuccessful() throws FileSystemOperationException, IOException {
    RewriteId rewriteId = RewriteId.from(RepositoryURL.from("https://test.com/owner/succes"));

    nioFileSystemAdapter.createDatastore(rewriteId);

    Path newDatastorePath = rewriteConfig.resolve(rewriteId);

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
    RewriteId rewriteId = RewriteId.from(RepositoryURL.from("https://test.com/owner/failing_datastore"));

    Path failingPath = tempWorkDirectory.resolve(rewriteId.get().toString());

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.createDirectories(failingPath)).thenThrow(new IOException("Permission denied"));
      mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);

      assertThatThrownBy(() -> nioFileSystemAdapter.createDatastore(rewriteId))
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
    RewriteId rewriteId = RewriteId.from(RepositoryURL.from("https://test.com/owner/to_delete_datastore"));
    Path datastoreToDelete = tempWorkDirectory.resolve(rewriteId.get().toString());
    Files.createDirectories(datastoreToDelete);
    Files.createFile(datastoreToDelete.resolve("file1.txt"));
    Files.createDirectories(datastoreToDelete.resolve("subdir"));
    Files.createFile(datastoreToDelete.resolve("subdir").resolve("file2.txt"));

    nioFileSystemAdapter.deleteDatastore(rewriteId);
    assertThat(Files.exists(datastoreToDelete)).isFalse();
    List<ILoggingEvent> logs = listAppender.list;

    assertThat(logs).isNotEmpty();
    assertThat(logs.stream().filter(l -> l.getLevel() == Level.INFO).map(ILoggingEvent::getFormattedMessage)).contains(
      "Datastore deleted: " + datastoreToDelete
    );
  }

  @Test
  void deleteDatastore_shouldLogWarn_whenDatastoreDoesNotExist() throws FileSystemOperationException {
    RewriteId rewriteId = RewriteId.from(RepositoryURL.from("https://test.com/owner/non_existent"));
    Path nonExistentDatastore = tempWorkDirectory.resolve(rewriteId.get().toString());

    nioFileSystemAdapter.deleteDatastore(rewriteId);
    assertThat(Files.exists(nonExistentDatastore)).isFalse();

    List<ILoggingEvent> logs = listAppender.list;
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getLevel()).isEqualTo(Level.WARN);
    assertThat(logs.get(0).getFormattedMessage()).contains("Datastore does not exist, skipping deletion: " + nonExistentDatastore);
  }

  @Test
  void deleteDatastore_shouldThrowException_whenIOExceptionOccursDuringDeletion() {
    RewriteId rewriteId = RewriteId.from(RepositoryURL.from("https://test.com/owner/failing_delete_dir"));
    Path failingDeleteDir = tempWorkDirectory.resolve(rewriteId.get().toString());
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

      assertThatThrownBy(() -> nioFileSystemAdapter.deleteDatastore(rewriteId))
        .isInstanceOf(FileSystemOperationException.class)
        .hasMessageContaining("Failed to delete Datastore: " + failingDeleteDir)
        .hasCauseInstanceOf(IOException.class);

      List<ILoggingEvent> logs = listAppender.list;
      assertThat(logs).isEmpty();
    }
  }

  @Test
  void listAllFiles_shouldReturnAllRegularFilesInDirectory() throws IOException, FileSystemOperationException {
    RewriteId rewriteId = RewriteId.from(RepositoryURL.from("https://test.com/owner/list_files_test"));
    Path testDir = tempWorkDirectory.resolve(rewriteId.get().toString());
    Files.createDirectories(testDir);
    Path file1 = Files.createFile(testDir.resolve("file1.txt"));
    Path file2 = Files.createFile(testDir.resolve("file2.log"));
    Files.createDirectories(testDir.resolve("subdir"));
    Files.createFile(testDir.resolve("subdir").resolve("nested.json"));

    Set<Path> files = nioFileSystemAdapter.listAllFiles(rewriteId);

    assertThat(files).containsExactlyInAnyOrder(file1, file2, testDir.resolve("subdir").resolve("nested.json"));
    assertThat(listAppender.list).isEmpty();
  }

  @Test
  void listAllFiles_shouldThrowException_whenPathIsNotDirectory() {
    RewriteId rewriteId = RewriteId.from(RepositoryURL.from("https://test.com/owner/not_a_dir"));
    Path filePath = tempWorkDirectory.resolve(rewriteId.get().toString());
    try {
      Files.createFile(filePath);
    } catch (IOException e) {
      throw new RuntimeException("Test setup failed", e);
    }

    assertThatThrownBy(() -> nioFileSystemAdapter.listAllFiles(rewriteId))
      .isInstanceOf(FileSystemOperationException.class)
      .hasMessageContaining("Path must be a directory: " + filePath);

    assertThat(listAppender.list).isEmpty();
  }

  @Test
  void listAllFiles_shouldThrowException_whenIOExceptionOccursDuringWalk() {
    RewriteId rewriteId = RewriteId.from(RepositoryURL.from("https://test.com/owner/failing_list_dir"));
    Path failingListDir = tempWorkDirectory.resolve(rewriteId.get().toString());
    try {
      Files.createDirectories(failingListDir);
    } catch (IOException e) {
      throw new RuntimeException("Test setup failed", e);
    }

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.isDirectory(failingListDir)).thenReturn(true);
      mockedFiles.when(() -> Files.walk(failingListDir)).thenThrow(new IOException("Read error"));

      assertThatThrownBy(() -> nioFileSystemAdapter.listAllFiles(rewriteId))
        .isInstanceOf(FileSystemOperationException.class)
        .hasMessageContaining("Failed to list files in datastore: " + failingListDir)
        .hasCauseInstanceOf(IOException.class);

      assertThat(listAppender.list).isEmpty();
    }
  }

  @Test
  void getDatastore_shouldReturnDatastore_whenPathExistsAndIsDirectory() throws IOException {
    RewriteId rewriteId = RewriteId.from(RepositoryURL.from("https://test.com/owner/get_datastore_success"));
    Path datastorePath = tempWorkDirectory.resolve(rewriteId.get().toString());
    Files.createDirectories(datastorePath);
    Path file1 = Files.createFile(datastorePath.resolve("config.json"));
    Path file2 = Files.createFile(datastorePath.resolve("data.txt"));
    Datastore datastore = nioFileSystemAdapter.getDatastore(rewriteId);
    assertThat(datastore).isNotNull();
    assertThat(datastore.rewriteId()).isEqualTo(rewriteId);
    assertThat(datastore.path()).isEqualTo(datastorePath);
    assertThat(datastore.files()).containsExactlyInAnyOrder(file1, file2);

    assertThat(listAppender.list).isEmpty();
  }

  @Test
  void getDatastore_shouldThrowDatastoreNotFoundException_whenPathDoesNotExist() {
    // Arrange
    RewriteId rewriteId = RewriteId.from(RepositoryURL.from("https://test.com/owner/not_found_datastorekkkk"));

    assertThatThrownBy(() -> nioFileSystemAdapter.getDatastore(rewriteId)).isInstanceOf(DatastoreNotFoundException.class);

    assertThat(listAppender.list).isEmpty();
  }

  @Test
  void getDatastore_shouldThrowDatastoreNotFoundException_whenPathExistsButIsNotDirectory() throws IOException {
    RewriteId rewriteId = RewriteId.from(RepositoryURL.from("https://test.com/owner/not_a_directory"));
    Path fileInsteadOfDir = tempWorkDirectory.resolve(rewriteId.get().toString());
    Files.createFile(fileInsteadOfDir); // Create a file instead of a directory

    assertThatThrownBy(() -> nioFileSystemAdapter.getDatastore(rewriteId))
      .isInstanceOf(FileSystemOperationException.class)
      .hasMessageContaining("Path must be a directory: " + fileInsteadOfDir);

    assertThat(listAppender.list).isEmpty();
  }

  @Test
  void getDatastore_shouldThrowMissingMandatoryValueException_whenRewriteIdIsNull() {
    RewriteId nullRewriteId = null;

    assertThatThrownBy(() -> nioFileSystemAdapter.getDatastore(nullRewriteId))
      .isInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining(NioFileSystemAdapter.REWRITE_ID);
  }

  @Test
  void getDatastore_shouldPropagateFileSystemOperationException_fromListAllFiles() throws IOException {
    RewriteId rewriteId = RewriteId.from(RepositoryURL.from("https://test.com/owner/list_error_during_get"));
    Path datastorePath = tempWorkDirectory.resolve(rewriteId.get().toString());
    Files.createDirectories(datastorePath);
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.exists(datastorePath)).thenReturn(true);
      mockedFiles.when(() -> Files.isDirectory(datastorePath)).thenReturn(true); // listAllFiles checks this
      mockedFiles.when(() -> Files.walk(datastorePath)).thenThrow(new IOException("Simulated file read error"));

      assertThatThrownBy(() -> nioFileSystemAdapter.getDatastore(rewriteId))
        .isInstanceOf(FileSystemOperationException.class)
        .hasMessageContaining("Failed to list files in datastore: " + datastorePath)
        .hasCauseInstanceOf(IOException.class);
    }
    assertThat(listAppender.list).isEmpty();
  }
}
