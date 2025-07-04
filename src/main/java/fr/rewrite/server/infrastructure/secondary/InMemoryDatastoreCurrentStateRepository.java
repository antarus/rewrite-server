package fr.rewrite.server.infrastructure.secondary;

import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentState;
import fr.rewrite.server.domain.datastore.projections.currentstate.DatastoreCurrentStateRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.Adapter;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class InMemoryDatastoreCurrentStateRepository implements DatastoreCurrentStateRepository, InMemorySecondaryAdapter {

  private final Map<DatastoreId, DatastoreCurrentState> views = new HashMap<>();

  @Override
  public void save(DatastoreCurrentState view) {
    views.put(view.datastoreId(), view);
  }

  @Override
  public Optional<DatastoreCurrentState> get(DatastoreId barcode) {
    return Optional.ofNullable(views.get(barcode));
  }

  @Override
  public List<DatastoreCurrentState> findAll() {
    return views.values().stream().toList();
  }

  @Override
  public void reset() {
    views.clear();
  }
}
