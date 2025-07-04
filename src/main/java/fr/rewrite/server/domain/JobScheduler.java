package fr.rewrite.server.domain;

import fr.rewrite.server.domain.datastore.DatastoreId;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface JobScheduler {
  <T> CompletableFuture<Void> submitJob(DatastoreId dsId, Supplier<Void> jobLogic);
}
