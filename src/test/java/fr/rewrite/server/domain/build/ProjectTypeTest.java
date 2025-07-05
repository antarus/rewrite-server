package fr.rewrite.server.domain.build;

import fr.rewrite.server.UnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class ProjectTypeTest {

    @Test
    void shouldHaveMavenType() {
        assertThat(ProjectType.MAVEN).isNotNull();
        assertThat(ProjectType.MAVEN.name()).isEqualTo("MAVEN");
    }

    @Test
    void shouldHaveGradleType() {
        assertThat(ProjectType.GRADLE).isNotNull();
        assertThat(ProjectType.GRADLE.name()).isEqualTo("GRADLE");
    }

    @Test
    void shouldHaveAntType() {
        assertThat(ProjectType.ANT).isNotNull();
        assertThat(ProjectType.ANT.name()).isEqualTo("ANT");
    }

    @Test
    void shouldHaveUnknownType() {
        assertThat(ProjectType.UNKNOWN).isNotNull();
        assertThat(ProjectType.UNKNOWN.name()).isEqualTo("UNKNOWN");
    }

    @Test
    void shouldContainAllExpectedTypes() {
        assertThat(ProjectType.values()).containsExactlyInAnyOrder(ProjectType.MAVEN, ProjectType.GRADLE, ProjectType.ANT, ProjectType.UNKNOWN);
    }
}
