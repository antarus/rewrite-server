package fr.rewrite.server.domain;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.shared.error.domain.Assert;
import java.nio.file.Path;

public record RewriteConfig(Path configDirectory, Path workDirectory, Path mvnPath, String dsCache, String dsRepository) {
  public static final String REWRITE_ID = "rewriteId";
  public static final String DATASTORE_ID = "datastoreId";

  public RewriteConfig {
    Assert.notNull("configDirectory", configDirectory);
    Assert.notNull("workDirectory", workDirectory);
    Assert.notNull("mvnPath", mvnPath);
    Assert.field("dsCache", dsCache).notBlank();
    Assert.field("dsRepository", dsRepository).notBlank();
  }
  public Path resolveDs(DatastoreId datastoreId) {
    Assert.notNull(DATASTORE_ID, datastoreId);
    return workDirectory().resolve(datastoreId.get().toString());
  }
  public Path resolveDsProject(DatastoreId datastoreId) {
    Assert.notNull(DATASTORE_ID, datastoreId);
    return Path.of(workDirectory.toString(), datastoreId.get().toString(), dsRepository);
  }

  public Path resolveDsCache(DatastoreId datastoreId) {
    Assert.notNull(DATASTORE_ID, datastoreId);
    return Path.of(workDirectory.toString(), datastoreId.get().toString(), dsCache);
  }

  public static final class RewriteConfigBuilder {

    private Path configDirectory;
    private Path workDirectory;
    private Path mvnPath;
    private String dsCache;
    private String dsRepsository;

    private RewriteConfigBuilder() {}

    public static RewriteConfigBuilder aRewriteConfig() {
      return new RewriteConfigBuilder();
    }

    public RewriteConfig build() {
      return new RewriteConfig(configDirectory, workDirectory, mvnPath, dsCache, dsRepsository);
    }

    public RewriteConfigBuilder configDirectory(String configDirectory) {
      Assert.field("configDirectory", configDirectory).notBlank();
      this.configDirectory = Path.of(configDirectory);
      return this;
    }

    public RewriteConfigBuilder workDirectory(String workDirectory) {
      Assert.field("workDirectory", workDirectory).notBlank();
      this.workDirectory = Path.of(workDirectory);
      return this;
    }

    public RewriteConfigBuilder mvnPath(String mvnPath) {
      Assert.field("mvnPath", mvnPath).notBlank();
      this.mvnPath = Path.of(mvnPath);
      return this;
    }

    public RewriteConfigBuilder dsCache(String dsCache) {
      this.dsCache = dsCache;
      return this;
    }

    public RewriteConfigBuilder dsRepsository(String dsProject) {
      this.dsRepsository = dsProject;
      return this;
    }
  }
}
