package fr.rewrite.server.domain.datastore;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.repository.RepositoryURL;
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

  @Test
  @DisplayName("from method should create Datastore successfully with valid parameters")
  void fromMethod_shouldCreateSuccessfully_withValidParameters() {
    RepositoryURL repositoryURL = createValidRepositoryURl();
    RewriteId rewriteId = createValidRewriteId(repositoryURL);
    Datastore datastore = Datastore.from(repositoryURL);
    assertThat(datastore).isNotNull();
    assertThat(datastore.rewriteId()).isEqualTo(rewriteId);
  }

  @Test
  @DisplayName("Datastore should be created successfully with an empty files set")
  void constructor_shouldCreateSuccessfully_withEmptyFilesSet() {
    RepositoryURL repositoryURL = createValidRepositoryURl();
    Datastore datastore = Datastore.from(repositoryURL);
    assertThat(datastore).isNotNull();
  }

  @Test
  @DisplayName("Equals and HashCode should work correctly for Datastore records")
  void equalsAndHashCode_shouldWorkCorrectly() {
    RepositoryURL repositoryURL1 = createValidRepositoryURl();
    RepositoryURL repositoryURL2 = createValidRepositoryURl2();

    Datastore datastore1 = Datastore.from(repositoryURL1);
    Datastore datastore1Copy = Datastore.from(repositoryURL1);

    Datastore datastore2 = Datastore.from(repositoryURL2);
    assertThat(datastore1).isEqualTo(datastore1Copy);
    assertThat(datastore1).isNotEqualTo(datastore2);

    assertThat(datastore1).isNotEqualTo(null);
    assertThat(datastore1).isNotEqualTo(new Object());

    assertThat(datastore1.hashCode()).isEqualTo(datastore1Copy.hashCode());

    assertThat(datastore1.hashCode()).isNotEqualTo(datastore2.hashCode());
  }
}
