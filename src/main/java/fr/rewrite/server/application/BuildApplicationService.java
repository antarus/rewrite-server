package fr.rewrite.server.application;

import fr.rewrite.server.domain.build.BuildExecution;
import fr.rewrite.server.domain.build.command.BuildProject;
import fr.rewrite.server.domain.build.command.GetClassPath;
import fr.rewrite.server.domain.datastore.DatastoreEventStore;
import fr.rewrite.server.domain.datastore.DatastoreNotFoundException;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentStateRepository;
import fr.rewrite.server.domain.repository.RepositoryExecution;
import fr.rewrite.server.domain.repository.command.RepositoryBranchCreate;
import fr.rewrite.server.domain.repository.command.RepositoryClone;
import fr.rewrite.server.domain.repository.command.RepositoryDelete;
import org.springframework.stereotype.Service;

@Service
public class BuildApplicationService {

  private final DatastoreCurrentStateRepository currentStateRepository;
  private final BuildExecution buildExecution;

  public BuildApplicationService(DatastoreCurrentStateRepository currentStateRepository, BuildExecution buildExecution) {
    this.currentStateRepository = currentStateRepository;
    this.buildExecution = buildExecution;
  }

  public void buildProject(BuildProject buildProject) {
    currentStateRepository.get(buildProject.datastoreId()).orElseThrow(() -> new DatastoreNotFoundException(buildProject.datastoreId()));

    buildExecution.buildProject(buildProject);
  }

  public void getClasspath(GetClassPath buildProject) {
    currentStateRepository.get(buildProject.datastoreId()).orElseThrow(() -> new DatastoreNotFoundException(buildProject.datastoreId()));

    buildExecution.getClasspath(buildProject);
  }
}
