package fr.rewrite.server.infrastructure.secondary.filesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.exception.DataAccessException;
import fr.rewrite.server.domain.state.RewriteConfig;
import fr.rewrite.server.domain.state.State;
import fr.rewrite.server.domain.state.StateEnum;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

@UnitTest
class JsonFileSystemRepositoryTest {

  @TempDir
  Path tempConfigDirectory;

  @TempDir
  Path tempWorkDirectory;

  private JsonFileSystemRepository repository;
  private RewriteConfig rewriteConfig;

  private ListAppender<ILoggingEvent> listAppender;
  private Logger logger;

  @BeforeEach
  void setUp() {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    logger = loggerContext.getLogger(JsonFileSystemRepository.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    logger.setLevel(Level.DEBUG);

    rewriteConfig = new RewriteConfig(tempConfigDirectory, tempWorkDirectory);
    repository = new JsonFileSystemRepository(rewriteConfig);
  }

  @AfterEach
  void tearDown() {
    logger.detachAppender(listAppender);
    listAppender.stop();
    listAppender.list.clear();
  }

  @Test
  @DisplayName("Constructor should create the config directory if it doesn't exist")
  void constructor_shouldCreateConfigDirectory() {
    assertThat(Files.exists(tempConfigDirectory)).isTrue();
    assertThat(Files.isDirectory(tempConfigDirectory)).isTrue();
  }

  @Test
  @DisplayName("Constructor should throw IllegalArgumentException if RewriteConfig.configDirectory is null")
  void constructor_shouldThrowIllegalArgumentExceptionIfConfigDirectoryIsNull() {
    MissingMandatoryValueException thrown = assertThrows(MissingMandatoryValueException.class, () -> {
      new RewriteConfig(null, tempWorkDirectory);
    });
    assertThat(thrown.getMessage()).contains("configDirectory");
  }

  @Test
  @DisplayName("Constructor should throw IllegalArgumentException if RewriteConfig.workDirectory is null")
  void constructor_shouldThrowIllegalArgumentExceptionIfWorkDirectoryIsNull() {
    MissingMandatoryValueException thrown = assertThrows(MissingMandatoryValueException.class, () -> {
      new RewriteConfig(tempConfigDirectory, null);
    });
    assertThat(thrown.getMessage()).contains("workDirectory");
  }

  @Test
  @DisplayName("Constructor should throw RewriteException if configDirectory creation fails")
  void constructor_shouldThrowRewriteExceptionIfConfigDirectoryCreationFails() throws IOException {
    Path invalidConfigPath = tempConfigDirectory.resolve("file.txt").resolve("subdir");
    Files.createFile(tempConfigDirectory.resolve("file.txt"));

    RewriteConfig invalidConfig = new RewriteConfig(invalidConfigPath, tempWorkDirectory);

    RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
      new JsonFileSystemRepository(invalidConfig);
    });
    assertThat(thrown.getMessage()).contains("Error when initilize base directory in JsonFileSystemRepository");
    assertThat(thrown.getCause()).isInstanceOf(IOException.class);
  }

  @Test
  @DisplayName("Save should write State record to file as JSON")
  void save_shouldWriteStateToFile() throws IOException {
    RewriteId id = new RewriteId(UUID.randomUUID());
    State state = new State(
      id,
      StateEnum.REPO_CLONED,
      Instant.now().minus(2, ChronoUnit.MINUTES),
      Instant.now().minus(5, ChronoUnit.MINUTES)
    );

    repository.save(state);

    Path expectedFilePath = tempConfigDirectory.resolve(id.get().toString() + ".json");
    assertThat(Files.exists(expectedFilePath)).isTrue();

    String fileContent = Files.readString(expectedFilePath);

    assertThat(fileContent).contains("\"status\" : \"" + state.status().name() + "\"");
    assertThat(fileContent).contains("\"createdAt\" : \"" + state.createdAt().toString() + "\"");
    assertThat(fileContent).contains("\"updatedAt\" : \"" + state.updatedAt().toString() + "\"");
    //    assertThat(listAppender.list).anyMatch(
    //      event ->
    //        event.getLevel() == Level.DEBUG &&
    //        event.getFormattedMessage().contains("Save Data for ID '" + id + "' in '" + expectedFilePath + "'")
    //    );
  }

  @Test
  @DisplayName("Save should throw DataAccessException if writing fails")
  void save_shouldThrowDataAccessExceptionIfWritingFails() {
    RewriteId id = new RewriteId(UUID.randomUUID());
    State state = State.init(id);

    assumeTrue(
      System.getProperty("os.name").toLowerCase().contains("linux") || System.getProperty("os.name").toLowerCase().contains("mac"),
      "Skipping permission-based test on non-Unix-like OS"
    );

    try {
      Set<PosixFilePermission> perms = PosixFilePermissions.fromString("r--r--r--");
      Files.setPosixFilePermissions(tempConfigDirectory, perms);
    } catch (IOException e) {
      System.err.println("Could not set POSIX file permissions for test setup: " + e.getMessage());
      assumeTrue(false, "Could not set file permissions for test, skipping.");
    }

    DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
      repository.save(state);
    });
    assertThat(thrown.getMessage()).isNotNull();
    assertThat(thrown.getCause()).isInstanceOf(IOException.class);
  }

  @Test
  @DisplayName("Get should return Optional with State record if file exists")
  void get_shouldReturnStateIfFileExists() throws IOException {
    RewriteId id = new RewriteId(UUID.randomUUID());
    State expectedState = new State(id, StateEnum.REPO_CREATED, Instant.now().minus(1, ChronoUnit.DAYS), Instant.now());

    Path filePath = tempConfigDirectory.resolve(id.get().toString() + ".json");
    ObjectMapper tempMapper = new ObjectMapper();
    tempMapper.registerModule(new JavaTimeModule());
    tempMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    tempMapper.writeValue(filePath.toFile(), expectedState);

    Optional<State> actualState = repository.get(id);

    assertThat(actualState).isPresent();
    assertThat(actualState.get().status()).isEqualTo(expectedState.status());
    //    assertThat(actualState.get().createdAt().withNano(0)).isEqualTo(expectedState.createdAt().withNano(0));
    //    assertThat(actualState.get().updatedAt().withNano(0)).isEqualTo(expectedState.updatedAt().withNano(0));

    assertThat(listAppender.list).anyMatch(
      event -> event.getLevel() == Level.DEBUG && event.getFormattedMessage().contains("Get Data for ID '" + id + "' in '" + filePath + "'")
    );
  }

  @Test
  @DisplayName("Get should return Optional.empty if file does not exist")
  void get_shouldReturnEmptyOptionalIfFileDoesNotExist() {
    RewriteId id = new RewriteId(UUID.randomUUID());

    Optional<State> actualState = repository.get(id);

    assertThat(actualState).isEmpty();

    Path expectedFilePath = tempConfigDirectory.resolve(id.get().toString() + ".json");
    assertThat(listAppender.list).anyMatch(
      event ->
        event.getLevel() == Level.DEBUG &&
        event.getFormattedMessage().contains("No data found for ID '" + id + "' in '" + expectedFilePath + "'")
    );
  }

  @Test
  @DisplayName("Get should throw DataAccessException if reading fails (e.g., malformed JSON)")
  void get_shouldThrowDataAccessExceptionIfReadingFails() throws IOException {
    RewriteId id = new RewriteId(UUID.randomUUID());
    Path filePath = tempConfigDirectory.resolve(id.get().toString() + ".json");

    Files.writeString(filePath, "{ \"invalid_json\": }");

    DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
      repository.get(id);
    });
    assertThat(thrown.getMessage()).isNotNull();
    assertThat(thrown.getCause()).isInstanceOf(com.fasterxml.jackson.core.JsonParseException.class);
  }

  @Test
  @DisplayName("Delete should remove the file if it exists")
  void delete_shouldDeleteFileIfExists() {
    RewriteId id = new RewriteId(UUID.randomUUID());
    State state = State.init(id);

    repository.save(state);
    Path filePath = tempConfigDirectory.resolve(id.get().toString() + ".json");
    assertThat(Files.exists(filePath)).isTrue();

    repository.delete(id);

    assertThat(Files.notExists(filePath)).isTrue();

    assertThat(listAppender.list).anyMatch(
      event ->
        event.getLevel() == Level.DEBUG && event.getFormattedMessage().contains("Delete data for ID '" + id + "' in '" + filePath + "'")
    );
  }

  @Test
  @DisplayName("Delete should do nothing and log debug if file does not exist")
  void delete_shouldNotThrowIfFileDoesNotExist() {
    RewriteId id = new RewriteId(UUID.randomUUID());
    Path filePath = tempConfigDirectory.resolve(id.get().toString() + ".json");

    repository.delete(id);
    assertThat(Files.notExists(filePath)).isTrue();

    assertThat(listAppender.list).anyMatch(
      event ->
        event.getLevel() == Level.DEBUG &&
        event.getFormattedMessage().contains("No data found for delete ID '" + id + "' in '" + filePath + "'")
    );
  }

  @Test
  @DisplayName("Delete should throw DataAccessException if deletion fails")
  void delete_shouldThrowDataAccessExceptionIfDeletionFails() throws IOException {
    RewriteId id = new RewriteId(UUID.randomUUID());
    Path filePath = tempConfigDirectory.resolve(id.get().toString() + ".json");
    Files.createFile(filePath);

    assumeTrue(
      System.getProperty("os.name").toLowerCase().contains("linux") || System.getProperty("os.name").toLowerCase().contains("mac"),
      "Skipping permission-based test on non-Unix-like OS"
    );

    try {
      Set<PosixFilePermission> perms = PosixFilePermissions.fromString("r-xr-xr-x");
      Files.setPosixFilePermissions(tempConfigDirectory, perms);
      System.out.println("Set tempConfigDirectory to read-only for deletion test.");
    } catch (IOException e) {
      System.err.println("Could not set POSIX file permissions for test setup: " + e.getMessage());
      assumeTrue(false, "Could not set directory permissions for test, skipping.");
    }

    DataAccessException thrown = assertThrows(DataAccessException.class, () -> {
      repository.delete(id);
    });
    assertThat(thrown.getMessage()).isNotNull();
    assertThat(thrown.getCause()).isInstanceOf(IOException.class);

    try {
      Files.setPosixFilePermissions(tempConfigDirectory, PosixFilePermissions.fromString("rwxrwxrwx"));
    } catch (IOException e) {
      System.err.println("Failed to restore permissions on temp directory: " + e.getMessage());
    }
  }
}
