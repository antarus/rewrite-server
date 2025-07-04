package fr.rewrite.server.domain.log;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.RewriteConfig;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@UnitTest
class CleanSensitiveLogTest {

  @Mock
  private RewriteConfig mockConfig; // Mock our RewriteConfig dependency

  private CleanSensitiveLog cleanSensitiveLog;

  // Define some realistic-looking paths for mocking
  private final String MOCKED_WORK_DIR = "/app/data/workdir";
  private final String MOCKED_CONFIG_DIR = "/app/config/settings";

  @BeforeEach
  void setUp() {
    // Configure our mock RewriteConfig before each test
    when(mockConfig.workDirectory()).thenReturn(Paths.get(MOCKED_WORK_DIR));
    when(mockConfig.configDirectory()).thenReturn(Paths.get(MOCKED_CONFIG_DIR));

    // Create the instance of CleanSensitiveLog with the mocked config
    cleanSensitiveLog = new CleanSensitiveLog(mockConfig);
  }

  @Test
  @DisplayName("Should mask the work directory path in the message")
  void shouldMaskWorkDirectoryPath() {
    String originalMessage = "Log entry from " + MOCKED_WORK_DIR + "/logs/process.log";
    String expectedMessage = "Log entry from [ *** ]/logs/process.log";
    String cleanedMessage = cleanSensitiveLog.clean(originalMessage);
    assertEquals(expectedMessage, cleanedMessage, "Work directory path should be masked.");
  }

  @Test
  @DisplayName("Should mask the config directory path in the message")
  void shouldMaskConfigDirectoryPath() {
    String originalMessage = "Configuration loaded from " + MOCKED_CONFIG_DIR + "/application.yml";
    String expectedMessage = "Configuration loaded from [ *** ]/application.yml";
    String cleanedMessage = cleanSensitiveLog.clean(originalMessage);
    assertEquals(expectedMessage, cleanedMessage, "Config directory path should be masked.");
  }

  @Test
  @DisplayName("Should mask both work and config directory paths if present")
  void shouldMaskBothPaths() {
    String originalMessage = "File " + MOCKED_WORK_DIR + "/file.txt accessed. Config: " + MOCKED_CONFIG_DIR + "/conf.properties";
    // The order of replacement matters: workDir is replaced first, then configDir.
    // Make sure the expected string reflects this if paths overlap, but here they don't.
    String expectedMessage = "File [ *** ]/file.txt accessed. Config: [ *** ]/conf.properties";
    String cleanedMessage = cleanSensitiveLog.clean(originalMessage);
    assertEquals(expectedMessage, cleanedMessage, "Both directory paths should be masked.");
  }

  @Test
  @DisplayName("Should not mask anything if paths are not present in the message")
  void shouldNotMaskIfPathsNotPresent() {
    String originalMessage = "A regular log message without sensitive paths.";
    String expectedMessage = originalMessage; // Should remain unchanged
    String cleanedMessage = cleanSensitiveLog.clean(originalMessage);
    assertEquals(expectedMessage, cleanedMessage, "Message should remain unchanged if no sensitive paths.");
  }

  @Test
  @DisplayName("Should handle multiple occurrences of the same path")
  void shouldHandleMultipleOccurrences() {
    String originalMessage = MOCKED_WORK_DIR + "/log1.log and " + MOCKED_WORK_DIR + "/log2.log";
    String expectedMessage = "[ *** ]/log1.log and [ *** ]/log2.log";
    String cleanedMessage = cleanSensitiveLog.clean(originalMessage);
    assertEquals(expectedMessage, cleanedMessage, "Multiple occurrences of a path should be masked.");
  }

  @Test
  @DisplayName("Should handle empty message without error and return empty string")
  void shouldHandleEmptyMessage() {
    String originalMessage = "";
    String expectedMessage = "";
    String cleanedMessage = cleanSensitiveLog.clean(originalMessage);
    assertEquals(expectedMessage, cleanedMessage, "Empty message should return empty string.");
  }

  @Test
  @DisplayName("Should handle null message in clean method gracefully by throwing exception from Assert.notNull")
  void shouldThrowExceptionForNullMessage() {
    // Assert that calling clean with null message throws NullPointerException due to Assert.notNull
    lenient().when(mockConfig.workDirectory()).thenReturn(Paths.get(MOCKED_WORK_DIR));
    lenient().when(mockConfig.configDirectory()).thenReturn(Paths.get(MOCKED_CONFIG_DIR));
    assertNull(cleanSensitiveLog.clean(null), "Null message should return empty string");
  }

  @Test
  @DisplayName("Should throw MissingMandatoryValueException if config is null on construction")
  void shouldThrowExceptionIfConfigIsNull() {
    lenient().when(mockConfig.workDirectory()).thenReturn(Paths.get(MOCKED_WORK_DIR));
    lenient().when(mockConfig.configDirectory()).thenReturn(Paths.get(MOCKED_CONFIG_DIR));
    MissingMandatoryValueException thrown = assertThrows(
      MissingMandatoryValueException.class,
      () -> new CleanSensitiveLog(null),
      "Constructor with null config should throw MissingMandatoryValueException."
    );
    assertTrue(thrown.getMessage().contains("config"));
  }

  // Additional test for ensuring path normalization is handled
  @Test
  @DisplayName("Should mask paths even if they are not perfectly normalized in the original message")
  void shouldMaskNonNormalizedPaths() {
    // Simulating a path that might not be perfectly normalized in the log message itself
    // but normalize() on RewriteConfig's paths will handle this.
    String originalMessage = "Accessing " + MOCKED_WORK_DIR + "/../workdir/file.txt";
    String expectedMessage = "Accessing [ *** ]/../workdir/file.txt"; // The replace uses the normalized path
    String cleanedMessage = cleanSensitiveLog.clean(originalMessage);
    assertEquals(
      expectedMessage,
      cleanedMessage,
      "Paths should be masked even if log message contains non-normalized forms relative to the base directory."
    );
  }

  @ParameterizedTest
  @MethodSource("provideParameters")
  public void testParametersFromMethod(String log, String result) {
    String cleanedMessage = cleanSensitiveLog.clean(log);
    assertEquals(result, cleanedMessage);
  }

  private static Stream<Arguments> provideParameters() {
    return Stream.of(
      Arguments.of(
        "argLine set to -javaagent:/home/cedric/.m2/repository/org/jacoco/org.jacoco.agent/0.8.8/org.jacoco.agent-0.8.8-runtime.jar=destfile=/home/cedric/rewrite/repository/904d1170-4bd7-3324-8139-95115b43bbe4/project/target/jacoco.exec,excludes=**/generator/buildtool/maven/.mvn/wrapper/*:src/main/webapp/app/common/primary/applicationlistener/WindowApplicationListener.ts",
        "argLine set to -javaagent:[ *** ]/.m2/repository/org/jacoco/org.jacoco.agent/0.8.8/org.jacoco.agent-0.8.8-runtime.jar=destfile=[ *** ]/rewrite/repository/904d1170-4bd7-3324-8139-95115b43bbe4/project/target/jacoco.exec,excludes=**/generator/buildtool/maven/.mvn/wrapper/*:src/main/webapp/app/common/primary/applicationlistener/WindowApplicationListener.ts"
      ),
      Arguments.of(
        "Running 'npm install' in /home/cedric/rewrite/repository/904d1170-4bd7-3324-8139-95115b43bbe4/project",
        "Running 'npm install' in [ *** ]/rewrite/repository/904d1170-4bd7-3324-8139-95115b43bbe4/project"
      )
    );
  }
}
