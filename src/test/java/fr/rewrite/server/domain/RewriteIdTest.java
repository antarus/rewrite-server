package fr.rewrite.server.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.rewrite.server.UnitTest;
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
  void fromString_shouldBeDeterministic() {
    String inputString1 = "https://github.com/my-org/repo-project-alpha";
    String inputString2 = "https://github.com/my-org/repo-project-alpha";
    String inputString3 = "https://github.com/another-org/another-repo";

    RewriteId id1 = RewriteId.fromString(inputString1);
    RewriteId id2 = RewriteId.fromString(inputString2);
    RewriteId id3 = RewriteId.fromString(inputString3);

    assertThat(id1.get()).isEqualTo(id2.get());
    assertThat(id1).isEqualTo(id2);

    assertThat(id1.get()).isNotEqualTo(id3.get());
    assertThat(id1).isNotEqualTo(id3);
  }

  @Test
  @DisplayName("fromString should return a valid UUID that can be re-parsed")
  void fromString_shouldReturnValidUuid() {
    String inputString = "some-unique-repository-name-or-url-12345";
    RewriteId rewriteId = RewriteId.fromString(inputString);

    assertThat(rewriteId).isNotNull();
    UUID generatedUuid = rewriteId.get();
    assertThat(generatedUuid).isNotNull();

    assertThat(UUID.fromString(generatedUuid.toString())).isEqualTo(generatedUuid);
  }

  @Test
  @DisplayName("fromString should throw MissingMandatoryValueException when input string is null")
  void fromString_shouldThrowMissingMandatoryValueException_whenInputIsNull() {
    assertThrows(MissingMandatoryValueException.class, () -> RewriteId.fromString(null));
  }

  @Test
  @DisplayName("fromString should throw MissingMandatoryValueException when input string is empty")
  void fromString_shouldThrowMissingMandatoryValueException_whenInputIsEmpty() {
    assertThrows(MissingMandatoryValueException.class, () -> RewriteId.fromString(""));
  }

  @Test
  @DisplayName("fromString should throw MissingMandatoryValueException when input string is blank")
  void fromString_shouldThrowMissingMandatoryValueException_whenInputIsBlank() {
    assertThrows(MissingMandatoryValueException.class, () -> RewriteId.fromString("   \t\n"));
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
