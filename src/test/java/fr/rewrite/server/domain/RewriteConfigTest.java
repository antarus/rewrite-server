package fr.rewrite.server.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class RewriteConfigTest {

  @Test
  void shouldNotBuildWithNullConfigDirectory() {
    assertThatThrownBy(() -> new RewriteConfig(null, Path.of("work"), Path.of("mvn"), "cache", "repo"))
      .isExactlyInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("configDirectory");
  }

  @Test
  void shouldNotBuildWithNullWorkDirectory() {
    assertThatThrownBy(() -> new RewriteConfig(Path.of("config"), null, Path.of("mvn"), "cache", "repo"))
      .isExactlyInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("workDirectory");
  }

  @Test
  void shouldNotBuildWithNullMvnPath() {
    assertThatThrownBy(() -> new RewriteConfig(Path.of("config"), Path.of("work"), null, "cache", "repo"))
      .isExactlyInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("mvnPath");
  }

  @Test
  void shouldNotBuildWithBlankDsCache() {
    assertThatThrownBy(() -> new RewriteConfig(Path.of("config"), Path.of("work"), Path.of("mvn"), "", "repo"))
      .isExactlyInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("dsCache");
  }

  @Test
  void shouldNotBuildWithBlankDsRepository() {
    assertThatThrownBy(() -> new RewriteConfig(Path.of("config"), Path.of("work"), Path.of("mvn"), "cache", ""))
      .isExactlyInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("dsRepository");
  }

  @Test
  void shouldResolveDs() {
    RewriteConfig config = new RewriteConfig(Path.of("config"), Path.of("work"), Path.of("mvn"), "cache", "repo");
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    assertThat(config.resolveDs(datastoreId)).isEqualTo(Path.of("work", datastoreId.get().toString()));
  }

  @Test
  void shouldResolveDsProject() {
    RewriteConfig config = new RewriteConfig(Path.of("config"), Path.of("work"), Path.of("mvn"), "cache", "repo");
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    assertThat(config.resolveDsProject(datastoreId)).isEqualTo(Path.of("work", datastoreId.get().toString(), "repo"));
  }

  @Test
  void shouldResolveDsCache() {
    RewriteConfig config = new RewriteConfig(Path.of("config"), Path.of("work"), Path.of("mvn"), "cache", "repo");
    DatastoreId datastoreId = new DatastoreId(UUID.randomUUID());
    assertThat(config.resolveDsCache(datastoreId)).isEqualTo(Path.of("work", datastoreId.get().toString(), "cache"));
  }
}
