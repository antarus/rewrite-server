package fr.rewrite.server.domain.build;

import fr.rewrite.server.domain.JobScheduler;
import fr.rewrite.server.domain.build.command.BuildProject;
import fr.rewrite.server.domain.build.command.GetClassPath;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.ddd.DomainService;
import fr.rewrite.server.domain.log.LogPublisher;
import fr.rewrite.server.shared.error.domain.Assert;

import java.util.concurrent.CompletableFuture;

@DomainService
public class BuildExecution {

    private final JobScheduler jobScheduler;
    private final BuildDomainService buildDomainService;
    private final LogPublisher logPublisher;

    public BuildExecution(JobScheduler jobScheduler,
                          BuildDomainService buildDomainService,
                          LogPublisher logPublisher) {
        this.jobScheduler = jobScheduler;
        this.buildDomainService = buildDomainService;
        this.logPublisher = logPublisher;
    }

    public CompletableFuture<Void> buildProject(BuildProject buildProject) {
        Assert.notNull("buildProject", buildProject);
        DatastoreId dsId = buildProject.datastoreId();

        return jobScheduler.submitJob(dsId, () -> {
            logPublisher.info("Starting build project job.", dsId);
            logPublisher.debug(String.format("Details:  ID=%s", dsId.uuid()), dsId);
            try {
                logPublisher.debug("Starting job in a Virtual Thread: " + Thread.currentThread(), dsId);

                buildDomainService.buildProject(buildProject);

                logPublisher.debug("Finished job in a Virtual Thread: " + Thread.currentThread(), dsId);

            } catch (Exception e) {
                logPublisher.error("Business error during build project: " + e.getMessage(), dsId);
                throw e;
            }
            return null;
        });
    }

    public CompletableFuture<Void> getClasspath(GetClassPath getClassPath) {
        Assert.notNull("getClassPath", getClassPath);
        DatastoreId dsId = getClassPath.datastoreId();

        return jobScheduler.submitJob(dsId, () -> {
            logPublisher.info("Starting get class path job.", dsId);
            logPublisher.debug(String.format("Details:  ID=%s", dsId.uuid()), dsId);
            try {
                logPublisher.debug("Starting job in a Virtual Thread: " + Thread.currentThread(), dsId);

                buildDomainService.getClassPath(getClassPath);

                logPublisher.debug("Finished job in a Virtual Thread: " + Thread.currentThread(), dsId);

            } catch (Exception e) {
                logPublisher.error("Business error during get class path: " + e.getMessage(), dsId);
                throw e;
            }
            return null;
        });
    }
}