package fr.rewrite.server.domain.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public interface DomainEvent extends Serializable {
  UUID eventId();
  Instant occurredOn();
}
