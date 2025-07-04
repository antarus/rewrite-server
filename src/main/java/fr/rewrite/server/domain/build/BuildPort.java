package fr.rewrite.server.domain.build;

import fr.rewrite.server.domain.datastore.DatastoreId;

import java.nio.file.Path;
import java.util.Set;

public interface BuildPort {

  void buildProject(DatastoreId datastoreId) ;
  Set<Path>  getClassPath(DatastoreId datastoreId);


}
