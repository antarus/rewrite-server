package fr.rewrite.server.shared.error.domain;

import static org.assertj.core.api.Assertions.*;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.shared.error.infrastructure.primary.RewriteServerExceptionFactory;
import java.util.Map;
import org.junit.jupiter.api.Test;

@UnitTest
class RewriteServerExceptionTest {

  @Test
  void shouldGetMinimalRewriteServerExceptionFromDomain() {
    RewriteServerException exception = RewriteServerException.builder(null).build();

    assertThat(exception.key()).isEqualTo(StandardErrorKey.INTERNAL_SERVER_ERROR);
    assertThat(exception.status()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR);
    assertThat(exception.getMessage()).isEqualTo("An error occurred");
    assertThat(exception.getCause()).isNull();
    assertThat(exception.parameters()).isEmpty();
  }

  @Test
  void shouldGetMinimalRewriteServerExceptionFromPrimary() {
    RewriteServerException exception = RewriteServerExceptionFactory.buildEmptyException();

    assertThat(exception.key()).isEqualTo(StandardErrorKey.INTERNAL_SERVER_ERROR);
    assertThat(exception.status()).isEqualTo(ErrorStatus.BAD_REQUEST);
    assertThat(exception.getMessage()).isEqualTo("An error occurred");
    assertThat(exception.getCause()).isNull();
    assertThat(exception.parameters()).isEmpty();
  }

  @Test
  void shouldGetFullRewriteServerException() {
    var cause = new RuntimeException();
    RewriteServerException exception = RewriteServerException.builder(StandardErrorKey.BAD_REQUEST)
      .message("This is an error")
      .cause(cause)
      .addParameter("parameter", "value")
      .addParameters(Map.of("key", "value"))
      .status(ErrorStatus.BAD_REQUEST)
      .build();

    assertThat(exception.key()).isEqualTo(StandardErrorKey.BAD_REQUEST);
    assertThat(exception.status()).isEqualTo(ErrorStatus.BAD_REQUEST);
    assertThat(exception.getMessage()).isEqualTo("This is an error");
    assertThat(exception.getCause()).isEqualTo(cause);
    assertThat(exception.parameters()).containsOnly(entry("parameter", "value"), entry("key", "value"));
  }

  @Test
  void shouldGetTechnicalErrorExceptionFromMessage() {
    RewriteServerException exception = RewriteServerException.technicalError("This is a problem");

    assertThat(exception.getMessage()).isEqualTo("This is a problem");
    assertThat(exception.key()).isEqualTo(StandardErrorKey.INTERNAL_SERVER_ERROR);
    assertThat(exception.status()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void shouldGetTechnicalErrorException() {
    var cause = new RuntimeException();
    RewriteServerException exception = RewriteServerException.technicalError("This is a problem", cause);

    assertThat(exception.getMessage()).isEqualTo("This is a problem");
    assertThat(exception.key()).isEqualTo(StandardErrorKey.INTERNAL_SERVER_ERROR);
    assertThat(exception.getCause()).isEqualTo(cause);
    assertThat(exception.status()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void shouldGetInternalServerErrorShortcut() {
    RewriteServerException exception = RewriteServerException.internalServerError(StandardErrorKey.INTERNAL_SERVER_ERROR).build();

    assertThat(exception.status()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void shouldGetBadRequestShortcut() {
    RewriteServerException exception = RewriteServerException.badRequest(StandardErrorKey.BAD_REQUEST).build();

    assertThat(exception.status()).isEqualTo(ErrorStatus.BAD_REQUEST);
  }
}
