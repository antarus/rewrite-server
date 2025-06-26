package fr.rewrite.server.wire.init;

import fr.rewrite.server.domain.state.StateRepository;
import fr.rewrite.server.infrastructure.secondary.filesystem.NioFileSystemAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class StartupListener {

  private static final Logger log = LoggerFactory.getLogger(StartupListener.class);
  private final StateRepository stateRepository;
  private final NioFileSystemAdapter fileSystemAdapter;

  public StartupListener(StateRepository stateRepository, NioFileSystemAdapter fileSystemAdapter) {
    this.stateRepository = stateRepository;
    this.fileSystemAdapter = fileSystemAdapter;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    log.info("Application ready");
    stateRepository
      .getAll()
      .forEach(state -> {
        if (!fileSystemAdapter.exists(state)) {
          log.info("Delete state {}, datastore no more exist", state.uuid().toString());
          stateRepository.delete(state);
        }
      });
  }
}
