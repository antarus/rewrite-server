package fr.rewrite.server.domain.events;

import java.io.Serializable;
import java.time.LocalDateTime;

public interface DomainEvent extends Serializable {
  String eventId();
  LocalDateTime occurredOn();
}
