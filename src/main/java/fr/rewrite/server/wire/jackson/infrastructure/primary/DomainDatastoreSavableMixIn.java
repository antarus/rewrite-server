package fr.rewrite.server.wire.jackson.infrastructure.primary;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.rewrite.server.domain.datastore.DatastoreClasspath;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
  {
    @JsonSubTypes.Type(value = DatastoreClasspath.class),
    // TODO IMPORTANT : Add all class who implement DatastoreSavable.
  }
)
interface DomainDatastoreSavableMixIn {}
