package fr.rewrite.server.infrastructure.secondary.buildtool;

import fr.rewrite.server.domain.BuildToolPort;
import fr.rewrite.server.domain.exception.BuildToolException;
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
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class MavenBuildToolAdapter implements BuildToolPort {

  @Override
  public void executeBuild(Path projectDir, String mavenExecutablePath) throws BuildToolException { // Changement
    String mvnCommand = (mavenExecutablePath != null && !mavenExecutablePath.isEmpty()) ? mavenExecutablePath : "mvn";
    List<String> command = Arrays.asList(mvnCommand, "clean", "install", "-DskipTests");

    System.out.println("Executing Maven build: " + String.join(" ", command) + " in " + projectDir);

    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.directory(projectDir.toFile());
    processBuilder.redirectErrorStream(true);

    try { // NOUVEAU bloc try-catch
      Process process = processBuilder.start();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println(line);
        }
      }

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new BuildToolException("Maven build failed with exit code: " + exitCode);
      }
      System.out.println("Maven build completed successfully.");
    } catch (IOException | InterruptedException e) { // Capture des exceptions techniques
      throw new BuildToolException("Error during Maven build execution: " + e.getMessage(), e);
    }
  }

  @Override
  public Set<Path> getProjectClasspath(Path projectDir, Path outputPath, String mavenExecutablePath) throws BuildToolException { // Changement
    String mvnCommand = (mavenExecutablePath != null && !mavenExecutablePath.isEmpty()) ? mavenExecutablePath : "mvn";
    List<String> command = Arrays.asList(
      mvnCommand,
      "dependency:build-classpath",
      "-Dmdep.outputFile=" + outputPath.getFileName().toString()
    );

    System.out.println("Generating Maven classpath: " + String.join(" ", command) + " in " + projectDir);

    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.directory(projectDir.toFile());
    processBuilder.redirectErrorStream(true);

    try { // NOUVEAU bloc try-catch
      Process process = processBuilder.start();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println(line);
        }
      }

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new BuildToolException("Maven classpath build failed with exit code: " + exitCode);
      }

      if (!Files.exists(outputPath)) {
        throw new BuildToolException("Classpath file not found at expected path: " + outputPath);
      }

      String classpathContent = Files.readString(outputPath);
      String separator = System.getProperty("path.separator");
      Set<Path> classpath = new HashSet<>();
      for (String pathString : classpathContent.split(separator)) {
        if (!pathString.trim().isEmpty()) {
          classpath.add(Paths.get(pathString.trim()));
        }
      }
      System.out.println("Maven classpath generated successfully with " + classpath.size() + " entries.");
      return classpath;
    } catch (IOException | InterruptedException e) { // Capture des exceptions techniques
      throw new BuildToolException("Error during Maven classpath generation: " + e.getMessage(), e);
    }
  }
}
