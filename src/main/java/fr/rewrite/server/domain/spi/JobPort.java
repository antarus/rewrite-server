package fr.rewrite.server.domain.spi;

import fr.rewrite.server.domain.RewriteId;

public interface JobPort {
  void createDatastoreJob(RewriteId rewriteId);
}
