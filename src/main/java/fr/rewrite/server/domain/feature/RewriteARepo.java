package fr.rewrite.server.domain.feature;

import fr.rewrite.server.domain.RewriteId;

public interface RewriteARepo {
  RewriteId createDatastore(String repoUrl);
}
