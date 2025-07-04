package fr.rewrite.server.infrastructure.secondary.filesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.RewriteConfig;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.datastore.DatastoreOperationException;
import fr.rewrite.server.domain.log.CleanSensitiveLog;
import fr.rewrite.server.domain.log.LogEntry;
import fr.rewrite.server.domain.log.LogPublisher;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.infrastructure.secondary.log.InMemoryLogStore;
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
import org.mockito.Mock;
import org.mockito.MockedStatic;

@UnitTest
class NioFileSystemAdapterTest {

  private NioFileSystemAdapter nioFileSystemAdapter;

  @TempDir
  Path tempConfigDirectory;

  @TempDir
  Path tempWorkDirectory;

  Path mvnPath = Path.of("/tmp/mvn");

  InMemoryLogStore inMemoryLogStore;

  private RewriteConfig rewriteConfig;

  private LogPublisher logPublisher;

  @Mock
  private CleanSensitiveLog cleanSensitiveLog;

  @BeforeEach
  void setUp() {
    rewriteConfig = RewriteConfig.RewriteConfigBuilder.aRewriteConfig()
      .configDirectory(tempConfigDirectory.toString())
      .workDirectory(tempWorkDirectory.toString())
      .mvnPath(mvnPath.toString())
      .dsCache(".datastore")
      .dsRepsository("project")
      .build();

    cleanSensitiveLog = new CleanSensitiveLog(rewriteConfig);
    inMemoryLogStore = new InMemoryLogStore();
    logPublisher = new LogPublisher(inMemoryLogStore, cleanSensitiveLog);

    // @Bean("objectMapperDs")
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(new Jdk8Module());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
      .allowIfBaseType("java.util.Set")
      .allowIfBaseType("java.util.List")
      .allowIfBaseType("java.util.Map")
      .allowIfSubTypeIsArray()
      .build();
    mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, JsonTypeInfo.As.PROPERTY);

    nioFileSystemAdapter = new NioFileSystemAdapter(logPublisher, rewriteConfig, mapper, cleanSensitiveLog);
  }

  @AfterEach
  void tearDown() {
    inMemoryLogStore.reset();
  }

  @Test
  void createDatastore_shouldCreateDirectory_whenSuccessful() throws DatastoreOperationException {
    DatastoreId datastoreId = DatastoreId.from(RepositoryURL.from("https://test.com/owner/succes"));

    nioFileSystemAdapter.provisionDatastore(datastoreId);

    Path newDatastorePath = rewriteConfig.resolveDs(datastoreId);

    assertThat(Files.isDirectory(newDatastorePath)).isTrue();

    List<LogEntry> logEntry = inMemoryLogStore.getLogsByDatastoreId(datastoreId);

    assertThat(logEntry).hasSize(2);
    assertThat(logEntry.get(0).level().toString()).hasToString(Level.DEBUG.toString());
    assertThat(logEntry.get(0).message()).contains("Creating datastore");
    assertThat(logEntry.get(1).level().toString()).hasToString(Level.INFO.toString());
    assertThat(logEntry.get(1).message()).contains(cleanSensitiveLog.clean("Directory created: " + newDatastorePath));
  }

  @Test
  void createDatastore_shouldThrowException_whenIOExceptionOccurs() {
    DatastoreId datastoreId = DatastoreId.from(RepositoryURL.from("https://test.com/owner/failing_datastore"));

    Path failingPath = tempWorkDirectory.resolve(datastoreId.get().toString());

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.createDirectories(failingPath)).thenThrow(new IOException("Permission denied"));
      mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);

      assertThatThrownBy(() -> nioFileSystemAdapter.provisionDatastore(datastoreId))
        .isInstanceOf(DatastoreOperationException.class)
        .hasMessageContaining("Failed to create datastore: " + (failingPath))
        .hasCauseInstanceOf(IOException.class);

      List<LogEntry> logEntry = inMemoryLogStore.getLogsByDatastoreId(datastoreId);
      assertThat(logEntry).hasSize(1);
      assertThat(logEntry.getFirst().level().toString()).hasToString(Level.DEBUG.toString());
      assertThat(logEntry.getFirst().message()).contains("Creating datastore");
    }
  }

  @Test
  void deleteDatastore_shouldDeleteDirectory_whenExistsAndSuccessful() throws IOException, DatastoreOperationException {
    DatastoreId datastoreId = DatastoreId.from(RepositoryURL.from("https://test.com/owner/to_delete_datastore"));
    Path datastoreToDelete = tempWorkDirectory.resolve(datastoreId.get().toString());
    Files.createDirectories(datastoreToDelete);
    Files.createFile(datastoreToDelete.resolve("file1.txt"));
    Files.createDirectories(datastoreToDelete.resolve("subdir"));
    Files.createFile(datastoreToDelete.resolve("subdir").resolve("file2.txt"));

    nioFileSystemAdapter.deleteDatastore(datastoreId);
    assertThat(Files.exists(datastoreToDelete)).isFalse();
    List<LogEntry> logs = inMemoryLogStore.getLogsByDatastoreId(datastoreId);

    assertThat(logs).isNotEmpty();
    assertThat(logs.stream().filter(l -> Level.INFO.toString().equals(l.level().toString())).map(LogEntry::message)).contains(
      cleanSensitiveLog.clean("Datastore deleted: " + datastoreToDelete)
    );
  }

  @Test
  void deleteDatastore_shouldLogWarn_whenDatastoreDoesNotExist() throws DatastoreOperationException {
    DatastoreId datastoreId = DatastoreId.from(RepositoryURL.from("https://test.com/owner/non_existent"));
    Path nonExistentDatastore = tempWorkDirectory.resolve(datastoreId.get().toString());

    nioFileSystemAdapter.deleteDatastore(datastoreId);
    assertThat(Files.exists(nonExistentDatastore)).isFalse();

    List<LogEntry> logs = inMemoryLogStore.getLogsByDatastoreId(datastoreId);
    assertThat(logs).hasSize(1);
    assertThat(logs.getFirst().level()).hasToString(Level.WARN.toString());
    assertThat(logs.getFirst().message()).contains(
      cleanSensitiveLog.clean("Datastore does not exist, skipping deletion: " + nonExistentDatastore)
    );
  }

  @Test
  void deleteDatastore_shouldThrowException_whenIOExceptionOccursDuringDeletion() {
    DatastoreId datastoreId = DatastoreId.from(RepositoryURL.from("https://test.com/owner/failing_delete_dir"));
    Path failingDeleteDir = tempWorkDirectory.resolve(datastoreId.get().toString());
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

      assertThatThrownBy(() -> nioFileSystemAdapter.deleteDatastore(datastoreId))
        .isInstanceOf(DatastoreOperationException.class)
        .hasMessageContaining("Failed to delete Datastore: " + (failingDeleteDir))
        .hasCauseInstanceOf(IOException.class);

      List<LogEntry> logs = inMemoryLogStore.getLogsByDatastoreId(datastoreId);
      assertThat(logs).isEmpty();
    }
  }

  @Test
  void listAllFiles_shouldReturnAllRegularFilesInDirectory() throws IOException, DatastoreOperationException {
    DatastoreId dsId = DatastoreId.from(RepositoryURL.from("https://test.com/owner/list_files_test"));
    Path testDir = tempWorkDirectory.resolve(dsId.get().toString());
    Files.createDirectories(testDir);
    Path file1 = Files.createFile(testDir.resolve("file1.txt"));
    Path file2 = Files.createFile(testDir.resolve("file2.log"));
    Files.createDirectories(testDir.resolve("subdir"));
    Files.createFile(testDir.resolve("subdir").resolve("nested.json"));

    Set<Path> files = nioFileSystemAdapter.listAllFiles(dsId);

    assertThat(files).containsExactlyInAnyOrder(file1, file2, testDir.resolve("subdir").resolve("nested.json"));
    assertThat(inMemoryLogStore.getLogsByDatastoreId(dsId)).isEmpty();
  }

  @Test
  void listAllFiles_shouldThrowException_whenPathIsNotDirectory() {
    DatastoreId datastoreId = DatastoreId.from(RepositoryURL.from("https://test.com/owner/not_a_dir"));
    Path filePath = tempWorkDirectory.resolve(datastoreId.get().toString());
    try {
      Files.createFile(filePath);
    } catch (IOException e) {
      throw new RuntimeException("Test setup failed", e);
    }

    assertThatThrownBy(() -> nioFileSystemAdapter.listAllFiles(datastoreId))
      .isInstanceOf(DatastoreOperationException.class)
      .hasMessageContaining("Path must be a directory: " + (filePath));

    assertThat(inMemoryLogStore.getLogsByDatastoreId(datastoreId)).isEmpty();
  }

  @Test
  void listAllFiles_shouldThrowException_whenIOExceptionOccursDuringWalk() {
    DatastoreId datastoreId = DatastoreId.from(RepositoryURL.from("https://test.com/owner/failing_list_dir"));
    Path failingListDir = tempWorkDirectory.resolve(datastoreId.get().toString());
    try {
      Files.createDirectories(failingListDir);
    } catch (IOException e) {
      throw new RuntimeException("Test setup failed", e);
    }

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.isDirectory(failingListDir)).thenReturn(true);
      mockedFiles.when(() -> Files.walk(failingListDir)).thenThrow(new IOException("Read error"));

      assertThatThrownBy(() -> nioFileSystemAdapter.listAllFiles(datastoreId))
        .isInstanceOf(DatastoreOperationException.class)
        .hasMessageContaining(cleanSensitiveLog.clean("Failed to list files in datastore: " + failingListDir))
        .hasCauseInstanceOf(IOException.class);

      assertThat(inMemoryLogStore.getLogsByDatastoreId(datastoreId)).isEmpty();
    }
  }
}
