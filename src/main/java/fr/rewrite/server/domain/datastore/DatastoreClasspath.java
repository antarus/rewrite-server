package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.shared.error.domain.Assert;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public record DatastoreClasspath(DatastoreId datastoreId, Set<String> classPath) implements DatastoreSavable {
  public DatastoreClasspath {
    Assert.notNull("datastoreId", datastoreId);
    Assert.field("classPath", classPath).notNull().noNullElement();
  }
  public static DatastoreClasspath from(DatastoreId datastoreId, Set<Path> classPath) {
    Assert.field("classPath", classPath).notNull().noNullElement();

    return new DatastoreClasspath(datastoreId, classPath.stream().map(Path::toString).collect(Collectors.toSet()));
  }
}
