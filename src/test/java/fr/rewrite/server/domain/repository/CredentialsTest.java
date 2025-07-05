package fr.rewrite.server.domain.repository;

import static org.junit.jupiter.api.Assertions.*;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import org.junit.jupiter.api.Test;

@UnitTest
class CredentialsTest {

  @Test
  void shouldCreateCredentialsSuccessfully() {
    String username = "testuser";
    String pat = "ghp_abcdef1234567890abcdef1234567890abcdef";

    Credentials credentials = new Credentials(username, pat);

    assertNotNull(credentials, "Credentials object should not be null.");
    assertEquals(username, credentials.username(), "Username should match the provided value.");
    assertEquals(pat, credentials.pat(), "PAT should match the provided value (direct access).");
  }

  @Test
  void toStringShouldMaskPat() {
    String username = "testuser";
    String pat = "ghp_abcdef1234567890abcdef1234567890abcdef";
    Credentials credentials = new Credentials(username, pat);

    String credentialsString = credentials.toString();

    assertTrue(credentialsString.contains("username='" + username + "'"), "toString() should include the username.");
    assertTrue(credentialsString.contains("pat='[ *** Mask ***]'"), "toString() should mask the PAT.");
    assertFalse(credentialsString.contains(pat), "toString() should NOT contain the unmasked PAT.");
  }

  @Test
  void toStringShouldMaskEmptyPat() {
    String username = "anotheruser";
    String pat = "";
    Credentials credentials = new Credentials(username, pat);

    String credentialsString = credentials.toString();

    assertTrue(credentialsString.contains("username='" + username + "'"), "toString() should include the username.");
    assertTrue(credentialsString.contains("pat='[ *** Mask ***]'"), "toString() should mask an empty PAT.");
  }

  @Test
  void shouldThrowNullPointerExceptionForNullUsername() {
    assertThrows(
      MissingMandatoryValueException.class,
      () -> new Credentials(null, "somepat"),
      "Constructor should throw NullPointerException for null username."
    );
  }

  @Test
  void shouldThrowNullPointerExceptionForNullPat() {
    assertThrows(
      MissingMandatoryValueException.class,
      () -> new Credentials("testuser", null),
      "Constructor should throw NullPointerException for null username."
    );
  }
}
