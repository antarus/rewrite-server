package fr.rewrite.server.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.repository.RepositoryURL;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class RewriteIdTest {

  @Test
  @DisplayName("Constructor should throw MissingMandatoryValueException when uuid is null")
  void constructor_shouldThrowMissingMandatoryValueException_whenUuidIsNull() {
    assertThrows(MissingMandatoryValueException.class, () -> new RewriteId(null));
  }

  @Test
  @DisplayName("Constructor should create RewriteId successfully with a valid UUID")
  void constructor_shouldCreateSuccessfully_withValidUuid() {
    UUID testUuid = UUID.randomUUID();
    RewriteId rewriteId = new RewriteId(testUuid);
    assertThat(rewriteId).isNotNull();
    assertThat(rewriteId.get()).isEqualTo(testUuid);
  }

  @Test
  @DisplayName("fromString should return a RewriteId with a deterministic UUID for the same input string")
  void from_shouldBeDeterministic() {
    RepositoryURL repositoryURL1 = RepositoryURL.from("https://github.com/my-org/repo-project-alpha");
    RepositoryURL repositoryURL2 = RepositoryURL.from("https://github.com/my-org/repo-project-alpha");
    RepositoryURL repositoryURL3 = RepositoryURL.from("https://github.com/another-org/another-repo");

    RewriteId id1 = RewriteId.from(repositoryURL1);
    RewriteId id2 = RewriteId.from(repositoryURL2);
    RewriteId id3 = RewriteId.from(repositoryURL3);

    assertThat(id1.get()).isEqualTo(id2.get());
    assertThat(id1).isEqualTo(id2);

    assertThat(id1.get()).isNotEqualTo(id3.get());
    assertThat(id1).isNotEqualTo(id3);
  }

  @Test
  @DisplayName("fromString should throw MissingMandatoryValueException when input string is null")
  void from_shouldThrowMissingMandatoryValueException_whenInputIsNull_RepositoryUrl() {
    assertThrows(MissingMandatoryValueException.class, () -> RewriteId.from((RepositoryURL) null));
  }

  @Test
  @DisplayName("fromString should throw MissingMandatoryValueException when input string is null")
  void from_shouldThrowMissingMandatoryValueException_whenInputIsNull_UUID() {
    assertThrows(MissingMandatoryValueException.class, () -> RewriteId.from((UUID) null));
  }

  @Test
  @DisplayName("get() should return the underlying UUID")
  void get_shouldReturnUnderlyingUuid() {
    UUID expectedUuid = UUID.randomUUID();
    RewriteId rewriteId = new RewriteId(expectedUuid);
    assertThat(rewriteId.get()).isEqualTo(expectedUuid);
  }

  @Test
  @DisplayName("Equals and HashCode should work correctly for RewriteId records")
  void equalsAndHashCode_shouldWorkCorrectly() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();

    RewriteId id1 = new RewriteId(uuid1);
    RewriteId id1Copy = new RewriteId(uuid1);
    RewriteId id2 = new RewriteId(uuid2);

    assertThat(id1).isEqualTo(id1Copy);
    assertThat(id1).isNotEqualTo(id2);
    assertThat(id1).isNotEqualTo(null);
    assertThat(id1).isNotEqualTo(new Object());

    assertThat(id1.hashCode()).isEqualTo(id1Copy.hashCode());
    assertThat(id1.hashCode()).isNotEqualTo(id2.hashCode());
  }
}
