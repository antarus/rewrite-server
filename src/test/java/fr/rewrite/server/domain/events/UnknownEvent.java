package fr.rewrite.server.domain.events;

import java.time.LocalDateTime;

public record UnknownEvent(String eventId, LocalDateTime occurredOn, String data) implements DomainEvent {}
