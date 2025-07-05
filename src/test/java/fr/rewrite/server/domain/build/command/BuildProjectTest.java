package fr.rewrite.server.domain.build.command;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@UnitTest
class BuildProjectTest {

    @Test
    void shouldCreateBuildProjectCommand() {
        DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
        BuildProject command = new BuildProject(datastoreId);

        assertThat(command.datastoreId()).isEqualTo(datastoreId);
    }

    @Test
    void shouldThrowExceptionWhenDatastoreIdIsNull() {
        assertThatThrownBy(() -> new BuildProject(null))
                .isInstanceOf(MissingMandatoryValueException.class)
                .hasMessageContaining("datastoreId");
    }
}
