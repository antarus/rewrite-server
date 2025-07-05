package fr.rewrite.server.domain.build;

import fr.rewrite.server.UnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class BuildErrorKeyTest {

    @Test
    void shouldReturnCorrectKeyForBuildOperationError() {
        assertThat(BuildErrorKey.BUILD_OPERATION_ERROR.get()).isEqualTo("build-operation-error");
    }

    @Test
    void shouldReturnCorrectKeyForBuildProjectFailed() {
        assertThat(BuildErrorKey.BUILD_PROJECT_FAILED.get()).isEqualTo("build-project-failed");
    }

    @Test
    void shouldReturnCorrectKeyForBuildClasspathFailed() {
        assertThat(BuildErrorKey.BUILD_CLASSPATH_FAILED.get()).isEqualTo("build-classpath-failed");
    }
}
