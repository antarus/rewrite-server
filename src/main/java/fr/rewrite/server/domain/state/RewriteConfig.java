package fr.rewrite.server.domain.state;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.shared.error.domain.Assert;
import java.nio.file.Path;

public record RewriteConfig(Path configDirectory, Path workDirectory, Path mvnPath) {
  public RewriteConfig {
    Assert.notNull("configDirectory", configDirectory);
    Assert.notNull("workDirectory", workDirectory);
    Assert.notNull("mvnPath", mvnPath);
  }

  public Path resolve(RewriteId rewriteId) {
    Assert.notNull("rewriteId", rewriteId);
    return workDirectory().resolve(rewriteId.get().toString());
  }

  public String maskWorkdirectory(Path pathToSanitize) {
    Assert.notNull("pathToSanitize", pathToSanitize);
    Path normalizedOriginalPath = pathToSanitize.normalize();
    Path normalizedWorkDirectory = workDirectory.normalize();

    if (normalizedOriginalPath.startsWith(normalizedWorkDirectory)) {
      Path relativePath = normalizedWorkDirectory.relativize(normalizedOriginalPath);
      // Construire la chaîne masquée
      return "[ *** ]/" + relativePath.toString().replace("\\", "/");
    } else {
      return normalizedOriginalPath.toString().replace("\\", "/");
    }
  }

  public static final class RewriteConfigBuilder {

    private Path configDirectory;
    private Path workDirectory;
    private Path mvnPath;

    private RewriteConfigBuilder() {}

    public static RewriteConfigBuilder aRewriteConfig() {
      return new RewriteConfigBuilder();
    }

    public RewriteConfig build() {
      return new RewriteConfig(configDirectory, workDirectory, mvnPath);
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
  }
}
