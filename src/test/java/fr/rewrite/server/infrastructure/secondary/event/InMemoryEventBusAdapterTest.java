package fr.rewrite.server.infrastructure.secondary.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.events.AnotherTestEvent;
import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.domain.events.TestEvent;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

@UnitTest
class InMemoryEventBusAdapterTest {

  private InMemoryEventBusAdapter eventBusAdapter;
  private ListAppender<ILoggingEvent> listAppender;
  private Logger logger;

  @BeforeEach
  void setUp() {
    eventBusAdapter = new InMemoryEventBusAdapter();

    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    logger = loggerContext.getLogger(InMemoryEventBusAdapter.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    logger.setLevel(Level.DEBUG);
  }

  @AfterEach
  void tearDown() {
    logger.detachAppender(listAppender);
    listAppender.stop();
    listAppender.list.clear();
  }

  @Test
  @DisplayName("Should initialize with empty subscriber maps")
  void constructor_shouldInitializeEmptySubscribers() {
    Map<Class<? extends DomainEvent>, List<Consumer<? extends DomainEvent>>> subscribersMap = (Map<
        Class<? extends DomainEvent>,
        List<Consumer<? extends DomainEvent>>
      >) ReflectionTestUtils.getField(eventBusAdapter, "subscribers");

    List<Consumer<DomainEvent>> globalSubscribersList = (List<Consumer<DomainEvent>>) ReflectionTestUtils.getField(
      eventBusAdapter,
      "globalSubscribers"
    );

    assertThat(subscribersMap).isEmpty();
    assertThat(globalSubscribersList).isEmpty();
  }

  @Test
  @DisplayName("Should subscribe a handler for a specific event type")
  void subscribe_shouldAddHandlerForEventType() {
    Consumer<TestEvent> handler = mock(Consumer.class);
    eventBusAdapter.subscribe(TestEvent.class, handler);

    assertThat(listAppender.list).anyMatch(
      event -> event.getLevel() == Level.INFO && event.getFormattedMessage().contains("Subscribed to event: TestEvent")
    );

    TestEvent event = TestEvent.from("Hello");
    eventBusAdapter.publish(event);
    verify(handler).accept(event);
  }

  @Test
  @DisplayName("Should subscribe multiple handlers for the same event type")
  void subscribe_shouldAddMultipleHandlersForEventType() {
    Consumer<TestEvent> handler1 = mock(Consumer.class);
    Consumer<TestEvent> handler2 = mock(Consumer.class);

    eventBusAdapter.subscribe(TestEvent.class, handler1);
    eventBusAdapter.subscribe(TestEvent.class, handler2);

    TestEvent event = TestEvent.from("Multiple handlers test");
    eventBusAdapter.publish(event);

    verify(handler1).accept(event);
    verify(handler2).accept(event);
  }

  @Test
  @DisplayName("Should subscribe a handler for all domain events")
  void subscribeAll_shouldAddGlobalHandler() {
    Consumer<DomainEvent> globalHandler = mock(Consumer.class);
    eventBusAdapter.subscribeAll(globalHandler);

    assertThat(listAppender.list).anyMatch(
      event -> event.getLevel() == Level.INFO && event.getFormattedMessage().contains("Subscribed to receive ALL domain events.")
    );

    TestEvent testEvent = TestEvent.from("Global event 1");
    AnotherTestEvent anotherEvent = new AnotherTestEvent(123);

    eventBusAdapter.publish(testEvent);
    eventBusAdapter.publish(anotherEvent);

    verify(globalHandler).accept(testEvent);
    verify(globalHandler).accept(anotherEvent);
  }

  @Test
  @DisplayName("Should publish an event to its specific handler")
  void publish_shouldDispatchToSpecificHandler() {
    Consumer<TestEvent> specificHandler = mock(Consumer.class);
    eventBusAdapter.subscribe(TestEvent.class, specificHandler);

    TestEvent event = TestEvent.from("Specific dispatch test");
    eventBusAdapter.publish(event);

    verify(specificHandler).accept(event);
    assertThat(listAppender.list).anyMatch(
      logEvent ->
        logEvent.getLevel() == Level.DEBUG && logEvent.getFormattedMessage().contains("Publishing event: TestEvent - " + event.eventId())
    );
  }

  @Test
  @DisplayName("Should publish an event to global handlers even if specific handler exists")
  void publish_shouldDispatchToGlobalHandlers_withSpecificHandler() {
    Consumer<TestEvent> specificHandler = mock(Consumer.class);
    Consumer<DomainEvent> globalHandler = mock(Consumer.class);

    eventBusAdapter.subscribe(TestEvent.class, specificHandler);
    eventBusAdapter.subscribeAll(globalHandler);

    TestEvent event = TestEvent.from("Specific and global dispatch test");
    eventBusAdapter.publish(event);

    verify(specificHandler).accept(event);
    verify(globalHandler).accept(event);
  }

  @Test
  @DisplayName("Should publish an event to global handlers when no specific handler exists")
  void publish_shouldDispatchToGlobalHandlers_noSpecificHandler() {
    Consumer<DomainEvent> globalHandler = mock(Consumer.class);
    eventBusAdapter.subscribeAll(globalHandler);

    TestEvent event = TestEvent.from("Only global dispatch test");
    eventBusAdapter.publish(event);

    verify(globalHandler).accept(event);
    verifyNoMoreInteractions(globalHandler);
  }

  @Test
  @DisplayName("Should log debug message when no handlers found for an event type")
  void publish_shouldLogDebugWhenNoHandlers() {
    TestEvent event = TestEvent.from("No handler for this event");
    eventBusAdapter.publish(event);

    assertThat(listAppender.list).anyMatch(
      logEvent ->
        logEvent.getLevel() == Level.DEBUG &&
        logEvent.getFormattedMessage().contains("No handler found for class fr.rewrite.server.domain.events.TestEvent - " + event.eventId())
    );
  }

  @Test
  @DisplayName("Should continue publishing to other specific handlers if one throws exception")
  void publish_shouldHandleExceptionInSpecificHandler() {
    Consumer<TestEvent> faultyHandler = mock(Consumer.class);
    Consumer<TestEvent> goodHandler = mock(Consumer.class);

    doThrow(new RuntimeException("Simulated specific handler error")).when(faultyHandler).accept(any(TestEvent.class));

    eventBusAdapter.subscribe(TestEvent.class, faultyHandler);
    eventBusAdapter.subscribe(TestEvent.class, goodHandler);

    TestEvent event = TestEvent.from("Error in specific handler test");
    eventBusAdapter.publish(event);

    verify(faultyHandler).accept(event);
    verify(goodHandler).accept(event);

    assertThat(listAppender.list).anyMatch(
      logEvent ->
        logEvent.getLevel() == Level.ERROR &&
        logEvent
          .getFormattedMessage()
          .contains("Error processing event " + event.eventId() + " with handler: Simulated specific handler error")
    );
  }

  @Test
  @DisplayName("Should continue publishing to other global handlers if one throws exception")
  void publish_shouldHandleExceptionInGlobalHandler() {
    Consumer<DomainEvent> faultyGlobalHandler = mock(Consumer.class);
    Consumer<DomainEvent> goodGlobalHandler = mock(Consumer.class);

    doThrow(new RuntimeException("Simulated global handler error")).when(faultyGlobalHandler).accept(any(DomainEvent.class));

    eventBusAdapter.subscribeAll(faultyGlobalHandler);
    eventBusAdapter.subscribeAll(goodGlobalHandler);

    TestEvent event = TestEvent.from("Error in global handler test");
    eventBusAdapter.publish(event);

    verify(faultyGlobalHandler).accept(event);
    verify(goodGlobalHandler).accept(event);

    assertThat(listAppender.list).anyMatch(
      logEvent ->
        logEvent.getLevel() == Level.ERROR &&
        logEvent
          .getFormattedMessage()
          .contains(
            "Error processing global event " + event.eventId() + " with handler for type TestEvent : Simulated global handler error"
          )
    );
  }

  @Test
  @DisplayName("Should correctly pass event type to global error log")
  void publish_globalErrorHandler_logsCorrectEventType() {
    Consumer<DomainEvent> faultyGlobalHandler = mock(Consumer.class);
    doThrow(new RuntimeException("Global error")).when(faultyGlobalHandler).accept(any(DomainEvent.class));
    eventBusAdapter.subscribeAll(faultyGlobalHandler);

    AnotherTestEvent event = new AnotherTestEvent(500);
    eventBusAdapter.publish(event);

    assertThat(listAppender.list).anyMatch(
      logEvent ->
        logEvent.getLevel() == Level.ERROR &&
        logEvent
          .getFormattedMessage()
          .contains("Error processing global event " + event.eventId() + " with handler for type AnotherTestEvent : Global error")
    );
  }

  @Test
  @DisplayName("Should ensure specific handlers are called before global handlers")
  void publish_orderOfExecution_specificThenGlobal() {
    Consumer<TestEvent> specificHandler = mock(Consumer.class);
    Consumer<DomainEvent> globalHandler = mock(Consumer.class);

    eventBusAdapter.subscribe(TestEvent.class, specificHandler);
    eventBusAdapter.subscribeAll(globalHandler);

    TestEvent event = TestEvent.from("Order test");
    eventBusAdapter.publish(event);
    verify(specificHandler).accept(event);
    verify(globalHandler).accept(event);
  }
}
