package fr.rewrite.server.domain.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.datastore.DatastoreCreatedEvent;
import fr.rewrite.server.domain.repository.BranchCreatedEvent;
import fr.rewrite.server.domain.repository.RepositoryClonedEvent;
import fr.rewrite.server.domain.state.State;
import fr.rewrite.server.domain.state.StateEnum;
import fr.rewrite.server.domain.state.StateRepository;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

@UnitTest
class DomainEventHandlerServiceTest {

  @Mock
  private StateRepository stateRepository;

  @Captor
  private ArgumentCaptor<State> stateCaptor;

  private DomainEventHandlerService domainEventHandlerService;
  private static final String DOMAIN_EVENTS_PACKAGE = "fr.rewrite.server.domain";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    domainEventHandlerService = new DomainEventHandlerService(stateRepository);
  }

  @Test
  void shouldUpdateStateToClonedOnRepositoryClonedEvent() {
    // Given
    RewriteId rewriteId = RewriteId.from(UUID.randomUUID());
    RepositoryClonedEvent event = RepositoryClonedEvent.from(rewriteId);
    State initialState = State.init(rewriteId);
    when(stateRepository.get(rewriteId)).thenReturn(Optional.of(initialState));

    // When
    domainEventHandlerService.handleEvent(event);

    // Then
    verify(stateRepository).save(stateCaptor.capture());
    State savedState = stateCaptor.getValue();
    assertThat(savedState.status()).isEqualTo(StateEnum.CLONED);
    assertThat(savedState.rewriteId()).isEqualTo(rewriteId);
  }

  @Test
  void shouldUpdateStateToBranchCreatedOnBranchCreatedEvent() {
    // Given
    RewriteId rewriteId = RewriteId.from(UUID.randomUUID());
    BranchCreatedEvent event = BranchCreatedEvent.from(rewriteId);
    State initialState = State.init(rewriteId).withStatus(StateEnum.BRANCH_CREATING);
    when(stateRepository.get(rewriteId)).thenReturn(Optional.of(initialState));

    // When
    domainEventHandlerService.handleEvent(event);

    // Then
    verify(stateRepository).save(stateCaptor.capture());
    State savedState = stateCaptor.getValue();
    assertThat(savedState.status()).isEqualTo(StateEnum.BRANCH_CREATED);
    assertThat(savedState.rewriteId()).isEqualTo(rewriteId);
  }

  @Test
  void allDomainEventsShouldHaveAHandler() throws NoSuchFieldException, IllegalAccessException {
    Reflections reflections = new Reflections(
      new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(DOMAIN_EVENTS_PACKAGE)).setScanners(Scanners.SubTypes)
    );
    Set<Class<? extends DomainEvent>> actualDomainEventClasses = reflections.getSubTypesOf(DomainEvent.class);

    actualDomainEventClasses.remove(DomainEvent.class);

    Set<Class<? extends DomainEvent>> ignoredTestEvents = new HashSet<>();
    ignoredTestEvents.add(TestEvent.class);
    ignoredTestEvents.add(AnotherTestEvent.class);
    ignoredTestEvents.add(UnknownEvent.class);
    ignoredTestEvents.add(LoggingEvent.class);

    actualDomainEventClasses.removeAll(ignoredTestEvents);

    Field handlersField = DomainEventHandlerService.class.getDeclaredField("handlers");
    handlersField.setAccessible(true);

    @SuppressWarnings("unchecked")
    Map<Class<?>, Consumer<DomainEvent>> handlers = (Map<Class<?>, Consumer<DomainEvent>>) handlersField.get(domainEventHandlerService);

    Set<Class<?>> registeredHandlers = handlers.keySet();

    assertThat(registeredHandlers)
      .as("All concrete DomainEvent classes should have a registered handler")
      .containsExactlyInAnyOrderElementsOf(actualDomainEventClasses);
  }

  @Test
  void handleEventShouldCallCorrectHandlerForDatastoreCreatedEvent() {
    DatastoreCreatedEvent event = mock(DatastoreCreatedEvent.class);
    when(event.rewriteId()).thenReturn(RewriteId.from(UUID.randomUUID()));

    domainEventHandlerService.handleEvent(event);

    // Verify that updateState was called (indirectly verifying handleDatastoreCreatedEvent was called)
    verify(stateRepository, times(1)).get(any());
  }

  @Test
  void handleEventShouldCallCorrectHandlerForRepositoryClonedEvent() {
    RepositoryClonedEvent event = mock(RepositoryClonedEvent.class);
    when(event.rewriteId()).thenReturn(RewriteId.from(UUID.randomUUID()));

    domainEventHandlerService.handleEvent(event);

    verify(stateRepository, times(1)).get(any());
  }

  @Test
  void handleEventShouldCallCorrectHandlerForBranchCreatedEvent() {
    BranchCreatedEvent event = mock(BranchCreatedEvent.class);
    when(event.rewriteId()).thenReturn(mock(fr.rewrite.server.domain.RewriteId.class)); // Mock RewriteId

    domainEventHandlerService.handleEvent(event);

    verify(stateRepository, times(1)).get(any());
  }
}
