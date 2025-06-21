package fr.rewrite.server.domain.spi;

import fr.rewrite.server.domain.RewriteId;

public interface JobSchedulerPort {
  void createDatastoreJob(RewriteId rewriteId);
}
