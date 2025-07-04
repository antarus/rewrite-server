package fr.rewrite.server.domain.datastore.event;

import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.datastore.DatastoreId;
import org.jmolecules.event.annotation.DomainEvent;

@DomainEvent
public sealed interface DatastoreEvent permits DatastoreCreateErrorEvent, DatastoreCreated, DatastoreDeleted, DatastoreStatusChanged {
  SequenceId sequenceId();

  DatastoreId datastoreId();
}
