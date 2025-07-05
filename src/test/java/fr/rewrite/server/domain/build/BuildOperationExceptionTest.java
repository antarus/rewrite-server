package fr.rewrite.server.domain.build;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.shared.error.domain.ErrorStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class BuildOperationExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "Test message";
        BuildOperationException exception = new BuildOperationException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.key()).isEqualTo(BuildErrorKey.BUILD_OPERATION_ERROR);
        assertThat(exception.status()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldCreateExceptionWithCause() {
        Throwable cause = new RuntimeException("Original cause");
        BuildOperationException exception = new BuildOperationException(cause);

        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.key()).isEqualTo(BuildErrorKey.BUILD_OPERATION_ERROR);
        assertThat(exception.status()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        String message = "Test message with cause";
        Throwable cause = new RuntimeException("Original cause");
        BuildOperationException exception = new BuildOperationException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.key()).isEqualTo(BuildErrorKey.BUILD_OPERATION_ERROR);
        assertThat(exception.status()).isEqualTo(ErrorStatus.INTERNAL_SERVER_ERROR);
    }
}
