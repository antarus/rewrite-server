package fr.rewrite.server.domain.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fr.rewrite.server.UnitTest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;

@UnitTest
class DomainEventHandlerServiceTest {

  private DomainEventHandlerService domainEventHandlerService;
  private ListAppender<ILoggingEvent> listAppender;
  private Logger logger;

  @Mock
  private Consumer<RepositoryCreatedEvent> mockRepositoryCreatedHandler;

  @BeforeEach
  void setUp() {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    logger = loggerContext.getLogger(DomainEventHandlerService.class.getName());
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    logger.setLevel(Level.DEBUG);

    MockitoAnnotations.openMocks(this);

    domainEventHandlerService = Mockito.spy(new DomainEventHandlerService());

    doNothing().when(domainEventHandlerService).handleLoggingEvent(any(LoggingEvent.class));
  }

  @AfterEach
  void tearDown() {
    logger.detachAppender(listAppender);
    listAppender.stop();
  }

  @Test
  void handleEvent_shouldLogInfo_whenNoHandlerRegistered() {
    DomainEvent unknownEvent = new UnknownEvent("dd", LocalDateTime.now(), "dsd");

    domainEventHandlerService.handleEvent(unknownEvent);

    List<ILoggingEvent> logs = listAppender.list;

    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getLevel()).isEqualTo(Level.INFO);
    assertThat(logs.get(0).getFormattedMessage()).contains(
      "DomainEventHandlerService: No specific handler registered for event type: UnknownEvent"
    );
    assertThat(logs.get(0).getArgumentArray()).containsExactly(unknownEvent.getClass().getSimpleName());
  }

  @Test
  void handleRepositoryCreatedEvent_shouldLogInfoWithEventPath() {
    RepositoryCreatedEvent event = new RepositoryCreatedEvent(UUID.randomUUID().toString(), LocalDateTime.now(), "/product/repo");

    doCallRealMethod().when(domainEventHandlerService).handleRepositoryCreatedEvent(event);
    domainEventHandlerService.handleEvent(event);

    List<ILoggingEvent> logs = listAppender.list;

    assertThat(logs).hasSize(2);
    assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
    assertThat(logs.get(1).getLevel()).isEqualTo(Level.INFO);
    assertThat(logs.get(1).getFormattedMessage()).contains("DomainEventHandlerService: Handling RepositoryCreatedEvent : " + event.path());
    assertThat(logs.get(1).getArgumentArray()).containsExactly(event.path());
  }

  @Test
  void handleEvent_shouldCallRepositoryCreatedEventHandler_whenRepositoryCreatedEvent() {
    RepositoryCreatedEvent event = new RepositoryCreatedEvent(UUID.randomUUID().toString(), LocalDateTime.now(), "/test/repo");
    doCallRealMethod().when(domainEventHandlerService).handleRepositoryCreatedEvent(event);

    Mockito.doAnswer(i -> {
      Consumer<DomainEvent> consumer = event1 -> domainEventHandlerService.handleRepositoryCreatedEvent(event);
      consumer.accept(event);
      return null;
    })
      .when(domainEventHandlerService)
      .handleEvent(event);
    domainEventHandlerService.handleEvent(event);

    verify(domainEventHandlerService, times(1)).handleRepositoryCreatedEvent(event);
    verify(domainEventHandlerService, never()).handleLoggingEvent(any(LoggingEvent.class));
  }

  @Test
  void handleEvent_shouldCallLoggingEventHandler_whenLoggingEvent() {
    LoggingEvent event = new LoggingEvent(UUID.randomUUID().toString(), LocalDateTime.now(), "Test log message");
    doCallRealMethod().when(domainEventHandlerService).handleLoggingEvent(event);

    Mockito.doAnswer(i -> {
      Consumer<DomainEvent> consumer = event1 -> domainEventHandlerService.handleLoggingEvent(event);
      consumer.accept(event);
      return null;
    })
      .when(domainEventHandlerService)
      .handleEvent(event);
    domainEventHandlerService.handleEvent(event);

    verify(domainEventHandlerService, times(1)).handleLoggingEvent(event);
    verify(domainEventHandlerService, never()).handleRepositoryCreatedEvent(any(RepositoryCreatedEvent.class));
  }

  @Test
  void handleEvent_shouldLogError_whenHandlerThrowsException() {
    RepositoryCreatedEvent event = new RepositoryCreatedEvent(UUID.randomUUID().toString(), LocalDateTime.now(), "/error/repo");

    doThrow(new RuntimeException("Simulated handler error")).when(mockRepositoryCreatedHandler).accept(event);
    domainEventHandlerService.registerHandler(RepositoryCreatedEvent.class, mockRepositoryCreatedHandler);
    domainEventHandlerService.handleEvent(event);
    verify(mockRepositoryCreatedHandler, times(1)).accept(event);

    List<ILoggingEvent> logs = listAppender.list;
    assertThat(logs).hasSize(2);
    assertThat(logs.get(0).getLevel()).isEqualTo(Level.DEBUG);
    assertThat(logs.get(1).getLevel()).isEqualTo(Level.ERROR);
    assertThat(logs.get(1).getFormattedMessage()).containsPattern(
      "Error processing event .+ of type RepositoryCreatedEvent : Simulated handler error"
    );
    assertThat(logs.get(1).getArgumentArray()).contains(event.eventId(), event.getClass().getSimpleName(), "Simulated handler error");
  }

  @Test
  void initHandlers_shouldRegisterKnownHandlers() {
    domainEventHandlerService = new DomainEventHandlerService();

    try {
      java.lang.reflect.Field handlersField = DomainEventHandlerService.class.getDeclaredField("handlers");
      handlersField.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<Class<? extends DomainEvent>, ?> handlers = (Map<Class<? extends DomainEvent>, ?>) handlersField.get(domainEventHandlerService);

      assertThat(handlers).containsKey(RepositoryCreatedEvent.class);
      assertThat(handlers).containsKey(LoggingEvent.class);
      assertThat(handlers).hasSize(2);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Failed to access handlers map for verification", e);
    }
  }

  @Test
  void handleLoggingEvent_shouldLogInfoWithLogMessage() {
    LoggingEvent event = new LoggingEvent(UUID.randomUUID().toString(), LocalDateTime.now(), "User logged in successfully");

    doCallRealMethod().when(domainEventHandlerService).handleLoggingEvent(event);

    domainEventHandlerService.handleEvent(event);

    List<ILoggingEvent> logs = listAppender.list;
    assertThat(logs).hasSize(2);
    assertThat(logs.get(1).getLevel()).isEqualTo(Level.INFO);
    assertThat(logs.get(1).getFormattedMessage()).contains("DomainEventHandlerService: Handling LoggingEvent : " + event.log());
    assertThat(logs.get(1).getArgumentArray()).containsExactly(event.log());
  }
}
