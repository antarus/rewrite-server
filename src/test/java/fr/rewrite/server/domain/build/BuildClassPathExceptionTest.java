package fr.rewrite.server.domain.build;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.DatastoreId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class BuildClassPathExceptionTest {

    @Test
    void shouldCreateExceptionWithDatastoreIdAndCause() {
        DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
        Exception cause = new RuntimeException("Test cause");

        BuildClassPathException exception = new BuildClassPathException(datastoreId, cause);

        assertThat(exception.key()).isEqualTo(BuildErrorKey.BUILD_OPERATION_ERROR);
        assertThat(exception.parameters().get("id")).isEqualTo(datastoreId.get().toString());
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
