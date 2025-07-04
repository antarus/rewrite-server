package fr.rewrite.server.domain.datastore.projections.currentstate;

import fr.rewrite.server.domain.datastore.DatastoreId;
import java.util.List;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.Port;

@Port
public interface DatastoreCurrentStateRepository {
  void save(DatastoreCurrentState view);

  Optional<DatastoreCurrentState> get(DatastoreId datastoreId);

  List<DatastoreCurrentState> findAll();
}
