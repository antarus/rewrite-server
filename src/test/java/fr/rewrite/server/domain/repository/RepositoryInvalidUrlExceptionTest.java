package fr.rewrite.server.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class RepositoryInvalidUrlExceptionTest {

  @Test
  void shouldCreateExceptionWithCorrectParameters() {
    String invalidUrl = "invalid-url";
    RepositoryInvalidUrlException exception = new RepositoryInvalidUrlException(invalidUrl);

    assertThat(exception.key()).isEqualTo(RepositoryErrorKey.REPOSITORY_INVALID_URI);
    assertThat(exception.parameters().get("repository.url")).isEqualTo(invalidUrl);
    assertThat(exception.getMessage()).contains("Url 'invalid-url' is not valid");
  }
}
