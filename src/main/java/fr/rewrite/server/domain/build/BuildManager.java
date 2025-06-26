package fr.rewrite.server.domain.build;

import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.exception.BuildToolException;

public interface BuildManager {
  void buildProject(RewriteId rewriteId) throws BuildToolException;

}
