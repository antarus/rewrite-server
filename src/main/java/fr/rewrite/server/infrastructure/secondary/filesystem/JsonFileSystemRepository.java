package fr.rewrite.server.infrastructure.secondary.filesystem;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.exception.DataAccessException;
import fr.rewrite.server.domain.state.RewriteConfig;
import fr.rewrite.server.domain.state.State;
import fr.rewrite.server.domain.state.StateRepository;
import fr.rewrite.server.shared.error.domain.Assert;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonFileSystemRepository implements StateRepository {

  private static final Logger log = LoggerFactory.getLogger(JsonFileSystemRepository.class);
  private final RewriteConfig rewriteConfig;
  private final ObjectMapper objectMapper;

  public JsonFileSystemRepository(RewriteConfig rewriteConfig) {
    Assert.notNull("rewriteConfig must not be null", rewriteConfig);
    this.objectMapper = new ObjectMapper();
    this.rewriteConfig = rewriteConfig;
    try {
      Files.createDirectories(this.rewriteConfig.configDirectory());
    } catch (Exception e) {
      throw new DataAccessException("Error when initilize base directory in JsonFileSystemRepository", e);
      // TODO
    }

    this.objectMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private Path getFilePath(RewriteId id) {
    return rewriteConfig.configDirectory().resolve(id.get().toString() + ".json");
  }

  @Override
  public void save(State data) throws DataAccessException {
    Assert.notNull("data", data);
    try {
      Path filePath = getFilePath(data.rewriteId());
      objectMapper.writeValue(filePath.toFile(), data);
      log.debug("Save Data for ID '{}' in '{}'", data.rewriteId().get(), filePath);
    } catch (Exception e) {
      throw new DataAccessException(e.getMessage(), e);
      // TODO
    }
  }

  @Override
  public Optional<State> get(RewriteId id) throws DataAccessException {
    try {
      Path filePath = getFilePath(id);
      if (Files.exists(filePath)) {
        State data = objectMapper.readValue(filePath.toFile(), State.class);
        log.debug("Get Data for ID '{}' in '{}'", id, filePath);
        return Optional.of(data);
      }
      log.debug("No data found for ID '{}' in '{}'", id, filePath);
      return Optional.empty();
    } catch (Exception e) {
      throw new DataAccessException(e.getMessage(), e);
      // TODO
    }
  }

  @Override
  public void delete(RewriteId id) throws DataAccessException {
    try {
      Path filePath = getFilePath(id);
      if (Files.exists(filePath)) {
        Files.delete(filePath);
        log.debug("Delete data for ID '{}' in '{}'", id, filePath);
      } else {
        log.debug("No data found for delete ID '{}' in '{}'", id, filePath);
      }
    } catch (Exception e) {
      throw new DataAccessException(e.getMessage(), e);
      //TODO
    }
  }
}
