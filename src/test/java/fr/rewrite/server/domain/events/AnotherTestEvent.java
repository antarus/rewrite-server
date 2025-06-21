package fr.rewrite.server.domain.events;

import fr.rewrite.server.UnitTest;
import java.time.LocalDateTime;
import java.util.UUID;

@UnitTest
public class AnotherTestEvent implements DomainEvent {

  private final String eventId;
  private final LocalDateTime occurredOn;
  private final int value;

  public AnotherTestEvent(int value) {
    this.eventId = UUID.randomUUID().toString();
    this.occurredOn = LocalDateTime.now();
    this.value = value;
  }

  @Override
  public String eventId() {
    return eventId;
  }

  @Override
  public LocalDateTime occurredOn() {
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
