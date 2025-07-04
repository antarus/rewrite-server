package fr.rewrite.server.infrastructure.secondary.build;

import fr.rewrite.server.domain.build.BuildOperationException;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.log.LogPublisher;
import fr.rewrite.server.domain.RewriteConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
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
    private final LogPublisher logPublisher;
    public MavenBuildTool(RewriteConfig rewriteConfig, LogPublisher logPublisher) {
        this.rewriteConfig = rewriteConfig;
        this.logPublisher = logPublisher;
    }

    @Override
    public void build(DatastoreId dsId) throws InterruptedException {
        Path datastorePath = rewriteConfig.resolveDsProject(dsId);
        String mvnCommand = (rewriteConfig.mvnPath() != null && Files.exists(rewriteConfig.mvnPath())) ? rewriteConfig.mvnPath().toString() : "mvn";
        List<String> command = Arrays.asList(mvnCommand, "clean", "install", "-DskipTests");

        logPublisher.info(String.format("Executing Maven build: %s in %s ", String.join(" ", command), datastorePath),dsId);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(datastorePath.toFile());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = startProcess(processBuilder, dsId);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new BuildOperationException("Maven build failed with exit code: " + exitCode);
            }
            logPublisher.info("Maven build completed successfully.",dsId);
        } catch (IOException e) {
            throw new BuildOperationException("Error during Maven build execution: " + e.getMessage(), e);
        }
    }

    private  @NotNull Process startProcess(ProcessBuilder processBuilder,DatastoreId dsId) throws IOException {
        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logPublisher.info(line,dsId);
            }
        }
        return process;
    }

    public Set<Path> getClassPath(DatastoreId dsId) throws BuildOperationException, InterruptedException {
        Set<Path> classpath = new HashSet<>();
        Path datastorePath = rewriteConfig.resolveDsProject(dsId);
        String mvnCommand = (rewriteConfig.mvnPath() != null && !Files.exists(rewriteConfig.mvnPath())) ? rewriteConfig.mvnPath().toString() : "mvn";
        ProcessBuilder processBuilder = new ProcessBuilder(
                mvnCommand,
                "dependency:build-classpath",
                "-Dmdep.outputFile=classpath.txt"
        );
        processBuilder.directory(datastorePath.toFile());
        processBuilder.redirectErrorStream(true);

        logPublisher.info("Building classpath using Maven...",dsId);
        try {
            Process process = startProcess(processBuilder,dsId);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new BuildOperationException("Maven classpath build failed with exit code: " + exitCode);
            }
            Path classpathFile = datastorePath.resolve("classpath.txt");
            if (Files.exists(classpathFile)) {
                String cpContent = Files.readString(classpathFile);
                for (String path : cpContent.split(File.pathSeparator)) {
                    Path p = Paths.get(path);
                    if (Files.exists(p)) {
                        classpath.add(p);
                    } else {
                        logPublisher.error("Classpath entry not found: " + p + ". This might indicate an issue with Maven's classpath generation or missing artifacts.",dsId                        );
                    }
                }
                Files.delete(classpathFile);
            }
        } catch (IOException e) {
            throw new BuildOperationException("Error during Maven classpath build execution: " + e.getMessage(), e);
        }
        return classpath;
    }


}


