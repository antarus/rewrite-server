package fr.rewrite.server.application;

import fr.rewrite.server.domain.datastore.DatastoreNotFoundException;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentStateRepository;
import fr.rewrite.server.domain.updater.UpdaterExecution;
import fr.rewrite.server.domain.updater.command.UpdaterCommand;
import org.springframework.stereotype.Service;

@Service
public class UpdaterApplicationService {

  private final DatastoreCurrentStateRepository currentStateRepository;
  private final UpdaterExecution updaterExecution;

  public UpdaterApplicationService(DatastoreCurrentStateRepository currentStateRepository, UpdaterExecution updaterExecution) {
    this.currentStateRepository = currentStateRepository;
    this.updaterExecution = updaterExecution;
  }

  public void updateProject(UpdaterCommand cmd) {
    currentStateRepository.get(cmd.datastoreId()).orElseThrow(() -> new DatastoreNotFoundException(cmd.datastoreId()));

    updaterExecution.updateProject(cmd);
  }
}
