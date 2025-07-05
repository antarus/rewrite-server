package fr.rewrite.server.domain;

import static org.mockito.Mockito.*;

import fr.rewrite.server.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class EventHandlerTest {

  @Test
  void shouldHandleEvent() {
    // Given
    Object event = new Object();
    EventHandler<Object> eventHandler = mock(EventHandler.class);

    // When
    eventHandler.handle(event);

    // Then
    verify(eventHandler, times(1)).handle(event);
  }
}
