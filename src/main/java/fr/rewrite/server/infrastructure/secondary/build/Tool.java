package fr.rewrite.server.infrastructure.secondary.build;

import fr.rewrite.server.domain.build.BuildOperationException;
import fr.rewrite.server.domain.datastore.DatastoreId;

import java.nio.file.Path;
import java.util.Set;

public interface Tool {
    void build(DatastoreId datastoreId) throws BuildOperationException, InterruptedException;
    Set<Path> getClassPath(DatastoreId datastoreId) throws BuildOperationException, InterruptedException;
}
