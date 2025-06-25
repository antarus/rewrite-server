package fr.rewrite.server.domain.events;

import fr.rewrite.server.UnitTest;
import java.time.Instant;
import java.util.UUID;

@UnitTest
public class AnotherTestEvent implements DomainEvent {

  private final UUID eventId;
  private final Instant occurredOn;
  private final int value;

  public AnotherTestEvent(int value) {
    this.eventId = UUID.randomUUID();
    this.occurredOn = Instant.now();
    this.value = value;
  }

  @Override
  public UUID eventId() {
    return eventId;
  }

  @Override
  public Instant occurredOn() {
    return occurredOn;
  }

  public int getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "AnotherTestEvent{" + "value=" + value + ", eventId='" + eventId() + '\'' + ", occurredOn=" + occurredOn + '}';
  }
}
