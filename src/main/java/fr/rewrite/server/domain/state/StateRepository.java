package fr.rewrite.server.domain.state;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.exception.DataAccessException;
import java.util.Collection;
import java.util.Optional;

public interface StateRepository {
  void save(State data) throws DataAccessException;
  Optional<State> get(RewriteId id) throws DataAccessException;
  void delete(RewriteId id) throws DataAccessException;
  Collection<RewriteId> getAll() throws DataAccessException;
}
