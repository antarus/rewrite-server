package fr.rewrite.server.domain.spi;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.State;
import fr.rewrite.server.domain.exception.DataAccessException;
import java.util.Optional;

public interface DataRepository {
  void save(State data) throws DataAccessException;
  Optional<State> get(RewriteId id) throws DataAccessException;
  void delete(RewriteId id) throws DataAccessException;
}
