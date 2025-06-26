package fr.rewrite.server.infrastructure.secondary.build;

import fr.rewrite.server.domain.datastore.Datastore;
import fr.rewrite.server.domain.exception.BuildToolException;
import fr.rewrite.server.domain.spi.BuildToolPort;
import fr.rewrite.server.domain.state.RewriteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MavenBuildTool implements Tool {
  private static final Logger log = LoggerFactory.getLogger(MavenBuildTool.class);
  private final RewriteConfig rewriteConfig;

    public MavenBuildTool(RewriteConfig rewriteConfig) {
        this.rewriteConfig = rewriteConfig;
    }

    @Override
  public void build(Datastore datastore) {
      Path datastorePath=rewriteConfig.resolve(datastore.rewriteId());
    String mvnCommand = (rewriteConfig.mvnPath() != null && !Files.exists(rewriteConfig.mvnPath())) ? rewriteConfig.mvnPath().toString() : "mvn";
    List<String> command = Arrays.asList(mvnCommand, "clean", "install", "-DskipTests");

      log.info("Executing Maven build: {} in {} ", String.join(" ", command) ,rewriteConfig.maskWorkdirectory( datastorePath));

    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.directory(datastorePath.toFile());
    processBuilder.redirectErrorStream(true);

    try {
      Process process = processBuilder.start();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          log.info(line);
        }
      }

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new BuildToolException("Maven build failed with exit code: " + exitCode);
      }
      log.info("Maven build completed successfully.");
    } catch (IOException | InterruptedException e) { // Capture des exceptions techniques
      throw new BuildToolException("Error during Maven build execution: " + e.getMessage(), e);
    }
  }

}
