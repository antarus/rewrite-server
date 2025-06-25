package fr.rewrite.server.wire.jackson.infrastructure.primary;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.rewrite.server.domain.datastore.DatastoreCreatedEvent;
import fr.rewrite.server.domain.events.LoggingEvent;
import fr.rewrite.server.domain.repository.RepositoryClonedEvent;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
  {
    @JsonSubTypes.Type(value = RepositoryClonedEvent.class),
    @JsonSubTypes.Type(value = LoggingEvent.class),
    @JsonSubTypes.Type(value = DatastoreCreatedEvent.class),
    // TODO IMPORTANT : Ajoute TOUS les records qui implémentent DomainEvent ici.
  }
)
interface DomainEventMixIn {}
