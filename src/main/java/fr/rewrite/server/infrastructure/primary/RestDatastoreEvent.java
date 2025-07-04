package fr.rewrite.server.infrastructure.primary;

public interface RestDatastoreEvent {
  RestSequenceId sequenceId();

  RestDatastoreId datastoreId();
}
