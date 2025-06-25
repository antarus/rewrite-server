package fr.rewrite.server.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.domain.repository.RepositoryURL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class DatastoreTest {

  private RepositoryURL createValidRepositoryURl() {
    return RepositoryURL.from("https://github.com/antarus/jdl");
  }

  private RepositoryURL createValidRepositoryURl2() {
    return RepositoryURL.from("https://github.com/antarus/jhicli");
  }

  private RewriteId createValidRewriteId(RepositoryURL url) {
    return RewriteId.from(url);
  }

  private RewriteId createValidRewriteId() {
    return new RewriteId(UUID.randomUUID());
  }

  private Path createValidPath() {
    return Path.of("/tmp/path/to/datastore/" + UUID.randomUUID().toString());
  }

  private Set<Path> createValidFilesSet() {
    Set<Path> files = new HashSet<>();
    files.add(Path.of("file1.txt"));
    files.add(Path.of("sub/file2.csv"));
    return files;
  }

  @Test
  @DisplayName("from method should create Datastore successfully with valid parameters")
  void fromMethod_shouldCreateSuccessfully_withValidParameters() {
    RepositoryURL repositoryURL = createValidRepositoryURl();
    RewriteId rewriteId = createValidRewriteId(repositoryURL);
    Path path = createValidPath();
    Set<Path> files = createValidFilesSet();

    Datastore datastore = Datastore.from(repositoryURL, path, files);

    assertThat(datastore).isNotNull();
    assertThat(datastore.rewriteId()).isEqualTo(rewriteId);
    assertThat(datastore.path()).isEqualTo(path);
    assertThat(datastore.files()).isEqualTo(files);
  }

  @Test
  @DisplayName("Datastore should be created successfully with an empty files set")
  void constructor_shouldCreateSuccessfully_withEmptyFilesSet() {
    RepositoryURL repositoryURL = createValidRepositoryURl();
    Path path = createValidPath();
    Set<Path> emptyFiles = Collections.emptySet();

    Datastore datastore = Datastore.from(repositoryURL, path, emptyFiles);

    assertThat(datastore).isNotNull();
    assertThat(datastore.files()).isEmpty();
  }

  @Test
  @DisplayName("Equals and HashCode should work correctly for Datastore records")
  void equalsAndHashCode_shouldWorkCorrectly() {
    RepositoryURL repositoryURL1 = createValidRepositoryURl();
    RewriteId rewriteId1 = createValidRewriteId(repositoryURL1);
    Path path1 = Path.of("/data/repo1");
    Set<Path> files1 = new HashSet<>(Set.of(Path.of("f1.txt")));
    RepositoryURL repositoryURL2 = createValidRepositoryURl2();
    RewriteId rewriteId2 = createValidRewriteId(repositoryURL2);
    Path path2 = Path.of("/data/repo2");
    Set<Path> files2 = new HashSet<>(Set.of(Path.of("f2.txt")));

    Datastore datastore1 = Datastore.from(repositoryURL1, path1, files1);
    Datastore datastore1Copy = Datastore.from(repositoryURL1, path1, files1);

    Datastore datastore2 = Datastore.from(repositoryURL1, path1, files2);
    Datastore datastore3 = Datastore.from(repositoryURL1, path2, files1);
    Datastore datastore4 = Datastore.from(repositoryURL2, path1, files1);

    assertThat(datastore1).isEqualTo(datastore1Copy);
    assertThat(datastore1).isNotEqualTo(datastore2);
    assertThat(datastore1).isNotEqualTo(datastore3);
    assertThat(datastore1).isNotEqualTo(datastore4);
    assertThat(datastore1).isNotEqualTo(null);
    assertThat(datastore1).isNotEqualTo(new Object());

    assertThat(datastore1.hashCode()).isEqualTo(datastore1Copy.hashCode());

    assertThat(datastore1.hashCode()).isNotEqualTo(datastore2.hashCode());
    assertThat(datastore1.hashCode()).isNotEqualTo(datastore3.hashCode());
    assertThat(datastore1.hashCode()).isNotEqualTo(datastore4.hashCode());
  }
}
