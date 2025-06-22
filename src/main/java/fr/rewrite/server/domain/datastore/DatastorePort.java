package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.exception.FileSystemOperationException;
import java.nio.file.Path;
import java.util.Set;

public interface DatastorePort {
  void createDatastore(RewriteId id) throws FileSystemOperationException;
  void deleteDatastore(RewriteId id) throws FileSystemOperationException;

  Datastore getDatastore(RewriteId id);

  Set<Path> listAllFiles(RewriteId id) throws FileSystemOperationException;
}
