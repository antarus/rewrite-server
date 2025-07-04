package fr.rewrite.server.domain.datastore;

import java.nio.file.Path;
import java.util.Set;
import org.jmolecules.architecture.hexagonal.Port;

@Port
public interface DatastorePort {
  void provisionDatastore(DatastoreId id) throws DatastoreOperationException;
  void deleteDatastore(DatastoreId id) throws DatastoreOperationException;

  void deleteRepository(DatastoreId datastoreId);

  boolean saveObjectToDsCache(DatastoreId ds, String filename, DatastoreSavable object);
  DatastoreSavable getObjectToDsCache(DatastoreId ds, String filename);

  Set<Path> listAllFiles(DatastoreId datastoreId) throws DatastoreOperationException;

  boolean exists(DatastoreId id);
}
