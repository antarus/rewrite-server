package fr.rewrite.server.infrastructure.secondary.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.domain.events.DomainEventHandlerService;
import fr.rewrite.server.domain.events.TestEvent;
import fr.rewrite.server.domain.spi.EventBusPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

@UnitTest
class EventServiceAdapterTest {

  private EventBusPort mockEventBus;
  private DomainEventHandlerService mockDomainEventHandlerService;
  private Environment mockEnvironment;

  private ListAppender<ILoggingEvent> listAppender;
  private Logger logger;

  @BeforeEach
  void setUp() {
    mockEventBus = mock(EventBusPort.class);
    mockDomainEventHandlerService = mock(DomainEventHandlerService.class);
    mockEnvironment = mock(Environment.class);

    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    logger = loggerContext.getLogger(EventServiceAdapter.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    logger.setLevel(Level.TRACE);
  }

  @AfterEach
  void tearDown() {
    logger.detachAppender(listAppender);
    listAppender.stop();
    listAppender.list.clear();
  }

  @Test
  @DisplayName("Should subscribe to all events when 'rabbit' profile is NOT active")
  void constructor_shouldSubscribeAllEvents_whenRabbitProfileNotActive() {
    when(mockEnvironment.getActiveProfiles()).thenReturn(new String[] { "dev", "test" });

    EventServiceAdapter eventServiceAdapter = new EventServiceAdapter(mockEventBus, mockDomainEventHandlerService, mockEnvironment);

    verify(mockEventBus, times(1)).subscribeAll(any());

    ArgumentCaptor<java.util.function.Consumer<DomainEvent>> consumerCaptor = ArgumentCaptor.forClass(java.util.function.Consumer.class);
    verify(mockEventBus).subscribeAll(consumerCaptor.capture());

    DomainEvent testEvent = TestEvent.from("Sample Event");
    consumerCaptor.getValue().accept(testEvent);

    verify(mockDomainEventHandlerService).handleEvent(testEvent);

    assertThat(listAppender.list).anyMatch(
      logEvent ->
        logEvent.getLevel() == Level.TRACE &&
        logEvent.getFormattedMessage().contains("EventServiceAdapter: Received DomainEvent " + testEvent.eventId())
    );
  }

  @Test
  @DisplayName("Should NOT subscribe to all events when 'rabbit' profile IS active")
  void constructor_shouldNotSubscribeAllEvents_whenRabbitProfileActive() {
    when(mockEnvironment.getActiveProfiles()).thenReturn(new String[] { "dev", "rabbit" });

    EventServiceAdapter eventServiceAdapter = new EventServiceAdapter(mockEventBus, mockDomainEventHandlerService, mockEnvironment);

    verify(mockEventBus, never()).subscribeAll(any());

    verifyNoInteractions(mockDomainEventHandlerService);
  }
}
