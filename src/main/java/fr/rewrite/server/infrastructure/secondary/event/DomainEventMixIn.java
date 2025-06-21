package fr.rewrite.server.infrastructure.secondary.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.rewrite.server.domain.events.LoggingEvent;
import fr.rewrite.server.domain.events.RepositoryCreatedEvent;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
  {
    @JsonSubTypes.Type(value = RepositoryCreatedEvent.class), @JsonSubTypes.Type(value = LoggingEvent.class),
    // TODO IMPORTANT : Ajoute TOUS les records qui implémentent DomainEvent ici.
  }
)
public interface DomainEventMixIn {}
