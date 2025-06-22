package fr.rewrite.server.domain.events;

import java.time.Instant;
import java.util.UUID;

public record UnknownEvent(UUID eventId, Instant occurredOn, String data) implements DomainEvent {}
