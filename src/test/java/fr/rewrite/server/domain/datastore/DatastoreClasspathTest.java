package fr.rewrite.server.domain.datastore;

import static org.junit.jupiter.api.Assertions.*;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import fr.rewrite.server.shared.error.domain.NullElementInCollectionException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class DatastoreClasspathTest {

  private final DatastoreId SAMPLE_DATASTORE_ID = new DatastoreId(UUID.randomUUID());

  @Test
  @DisplayName("Should create DatastoreClasspath successfully from Strings")
  void constructor_shouldCreateSuccessfully_fromStrings() {
    Set<String> classpathStrings = Set.of("path/to/jar1.jar", "path/to/jar2.jar");
    DatastoreClasspath classpath = new DatastoreClasspath(SAMPLE_DATASTORE_ID, classpathStrings);

    assertNotNull(classpath);
    assertEquals(SAMPLE_DATASTORE_ID, classpath.datastoreId());
    assertEquals(classpathStrings, classpath.classPath());
  }

  @Test
  @DisplayName("Should create DatastoreClasspath successfully from Paths")
  void fromMethod_shouldCreateSuccessfully_fromPaths() {
    Set<Path> classpathPaths = Set.of(Paths.get("path/to/libA.jar"), Paths.get("path/to/libB.jar"));
    DatastoreClasspath classpath = DatastoreClasspath.from(SAMPLE_DATASTORE_ID, classpathPaths);

    Set<String> expectedStrings = classpathPaths.stream().map(Path::toString).collect(Collectors.toSet());

    assertNotNull(classpath);
    assertEquals(SAMPLE_DATASTORE_ID, classpath.datastoreId());
    assertEquals(expectedStrings, classpath.classPath());
  }

  @Test
  @DisplayName("Should handle empty classpath set for constructor")
  void constructor_shouldHandleEmptyClasspath() {
    Set<String> emptyClasspath = Collections.emptySet();
    DatastoreClasspath classpath = new DatastoreClasspath(SAMPLE_DATASTORE_ID, emptyClasspath);

    assertNotNull(classpath);
    assertTrue(classpath.classPath().isEmpty());
  }

  @Test
  @DisplayName("Should handle empty classpath set for from method")
  void fromMethod_shouldHandleEmptyClasspath() {
    Set<Path> emptyClasspath = Collections.emptySet();
    DatastoreClasspath classpath = DatastoreClasspath.from(SAMPLE_DATASTORE_ID, emptyClasspath);

    assertNotNull(classpath);
    assertTrue(classpath.classPath().isEmpty());
  }

  @Test
  @DisplayName("Constructor should throw MissingMandatoryValueException if datastoreId is null")
  void constructor_shouldThrowException_whenDatastoreIdIsNull() {
    assertThrows(
      MissingMandatoryValueException.class,
      () -> new DatastoreClasspath(null, Set.of("some/path.jar")),
      "Should throw MissingMandatoryValueException if datastoreId is null."
    );
  }

  @Test
  @DisplayName("Constructor should throw NullElementInCollectionException if classPath contains null elements")
  void constructor_shouldThrowException_whenClassPathContainsNullElement() {
    Set<String> invalidClasspath = new HashSet<>();
    invalidClasspath.add("path/to/valid.jar");
    invalidClasspath.add(null); // Null element

    assertThrows(
      NullElementInCollectionException.class,
      () -> new DatastoreClasspath(SAMPLE_DATASTORE_ID, invalidClasspath),
      "Should throw NullElementInCollectionException if classPath contains null elements."
    );
  }

  @Test
  @DisplayName("from method should throw NullElementInCollectionException if classPath contains null elements")
  void fromMethod_shouldThrowException_whenClassPathContainsNullElement() {
    Set<Path> invalidClasspath = new HashSet<>();
    invalidClasspath.add(Paths.get("path/to/valid.jar"));
    invalidClasspath.add(null); // Null element

    assertThrows(
      NullElementInCollectionException.class,
      () -> DatastoreClasspath.from(SAMPLE_DATASTORE_ID, invalidClasspath),
      "Should throw NullElementInCollectionException if classPath contains null elements."
    );
  }

  @Test
  @DisplayName("Constructor should throw MissingMandatoryValueException if classPath set itself is null")
  void constructor_shouldThrowException_whenClassPathSetIsNull() {
    assertThrows(
      MissingMandatoryValueException.class,
      () -> new DatastoreClasspath(SAMPLE_DATASTORE_ID, null),
      "Should throw MissingMandatoryValueException if classPath set is null."
    );
  }

  @Test
  @DisplayName("from method should throw MissingMandatoryValueException if classPath set itself is null")
  void fromMethod_shouldThrowException_whenClassPathSetIsNull() {
    assertThrows(
      MissingMandatoryValueException.class,
      () -> DatastoreClasspath.from(SAMPLE_DATASTORE_ID, null),
      "Should throw MissingMandatoryValueException if classPath set is null."
    );
  }

  @Test
  @DisplayName("Should preserve uniqueness of classpath entries from Paths")
  void fromMethod_shouldPreserveUniqueness() {
    Set<Path> classpathPaths = Stream.of(
      Paths.get("path/to/duplicate.jar"),
      Paths.get("path/to/unique.jar"),
      Paths.get("path/to/duplicate.jar") // Duplicate
    ).collect(Collectors.toSet()); // Set naturally handles uniqueness

    DatastoreClasspath classpath = DatastoreClasspath.from(SAMPLE_DATASTORE_ID, classpathPaths);

    assertEquals(2, classpath.classPath().size(), "Set should only contain unique elements.");
    assertTrue(classpath.classPath().contains("path/to/duplicate.jar"));
    assertTrue(classpath.classPath().contains("path/to/unique.jar"));
  }
}
