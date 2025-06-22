package fr.rewrite.server.domain.state;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.shared.error.domain.Assert;
import java.nio.file.Path;

public record RewriteConfig(Path configDirectory, Path workDirectory) {
  public RewriteConfig {
    Assert.notNull("configDirectory", configDirectory);
    Assert.notNull("workDirectory", workDirectory);
  }

  public Path resolve(RewriteId rewriteId) {
    Assert.notNull("rewriteId", rewriteId);
    return workDirectory().resolve(rewriteId.get().toString());
  }

  public static final class RewriteConfigBuilder {

    private Path configDirectory;
    private Path workDirectory;

    private RewriteConfigBuilder() {}

    public static RewriteConfigBuilder aRewriteConfig() {
      return new RewriteConfigBuilder();
    }

    public RewriteConfig build() {
      return new RewriteConfig(configDirectory, workDirectory);
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
  }
}
