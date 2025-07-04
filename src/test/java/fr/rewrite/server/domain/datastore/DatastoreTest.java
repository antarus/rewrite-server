package fr.rewrite.server.domain.datastore;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.command.DatastoreCreation;
import fr.rewrite.server.domain.datastore.event.DatastoreCreated;
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

  private DatastoreId createValidRewriteId(RepositoryURL url) {
    return DatastoreId.from(url);
  }

  @Test
  @DisplayName("from method should create Datastore successfully with valid parameters")
  void fromMethod_shouldCreateSuccessfully_withValidParameters() {
    RepositoryURL repositoryURL = createValidRepositoryURl();
    DatastoreId datastoreId = createValidRewriteId(repositoryURL);
    DatastoreCreated datastore = Datastore.create(new DatastoreCreation(repositoryURL));
    assertThat(datastore).isNotNull();
    assertThat(datastore.datastoreId()).isEqualTo(datastoreId);
  }

  @Test
  @DisplayName("Datastore should be created successfully with an empty files set")
  void constructor_shouldCreateSuccessfully_withEmptyFilesSet() {
    RepositoryURL repositoryURL = createValidRepositoryURl();
    DatastoreCreated datastore = Datastore.create(new DatastoreCreation(repositoryURL));
    assertThat(datastore).isNotNull();
  }

  @Test
  @DisplayName("Equals and HashCode should work correctly for Datastore records")
  void equalsAndHashCode_shouldWorkCorrectly() {
    RepositoryURL repositoryURL1 = createValidRepositoryURl();
    RepositoryURL repositoryURL2 = createValidRepositoryURl2();

    DatastoreCreated datastore1 = Datastore.create(new DatastoreCreation(repositoryURL1));
    DatastoreCreated datastore1Copy = Datastore.create(new DatastoreCreation(repositoryURL1));

    DatastoreCreated datastore2 = Datastore.create(new DatastoreCreation(repositoryURL2));
    assertThat(datastore1.datastoreId()).isEqualTo(datastore1Copy.datastoreId());
    assertThat(datastore1.repositoryURL()).isEqualTo(datastore1Copy.repositoryURL());
    assertThat(datastore1).isNotEqualTo(datastore2);
    assertThat(datastore1).isNotEqualTo(null);

    assertThat(datastore1).isNotEqualTo(new Object());
  }
}
