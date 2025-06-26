package fr.rewrite.server.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.spi.JobPort;
import fr.rewrite.server.domain.state.State;
import fr.rewrite.server.domain.state.StateEnum;
import fr.rewrite.server.domain.state.StateRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@UnitTest
class RepositoryManagerImplTest {

  @Mock
  private JobPort jobPort;

  @Mock
  private StateRepository stateRepository;

  @Captor
  private ArgumentCaptor<State> stateCaptor;

  private RepositoryManager repositoryManager;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    repositoryManager = new RepositoryManagerImpl(jobPort, stateRepository);
  }

  @Test
  void shouldTransitionToCloningAndEnqueueJob() {
    // Given
    RepositoryURL repositoryURL = RepositoryURL.from("https://github.com/test/repo");
    RewriteId rewriteId = RewriteId.from(repositoryURL);
    State initialState = State.init(rewriteId);
    when(stateRepository.get(rewriteId)).thenReturn(Optional.of(initialState));

    // When
    repositoryManager.cloneRepository(repositoryURL, null);

    // Then
    verify(stateRepository).save(stateCaptor.capture());
    State savedState = stateCaptor.getValue();
    assertThat(savedState.status()).isEqualTo(StateEnum.CLONING);
    assertThat(savedState.rewriteId()).isEqualTo(rewriteId);
    verify(jobPort).cloneRepository(repositoryURL);
  }

  @Test
  void shouldTransitionToBranchCreatingAndEnqueueJob() {
    // Given
    RewriteId rewriteId = RewriteId.from(UUID.randomUUID());
    State currentState = State.init(rewriteId).withStatus(StateEnum.CLONED);
    when(stateRepository.get(rewriteId)).thenReturn(Optional.of(currentState));

    // When
    repositoryManager.createBranch(rewriteId, "new-branch");

    // Then
    verify(stateRepository).save(stateCaptor.capture());
    State savedState = stateCaptor.getValue();
    assertThat(savedState.status()).isEqualTo(StateEnum.BRANCH_CREATING);
    assertThat(savedState.rewriteId()).isEqualTo(rewriteId);
    verify(jobPort).createBranch(rewriteId, "new-branch");
  }

  @Test
  void shouldThrowExceptionForInvalidTransition() {
    // Given
    RewriteId rewriteId = RewriteId.from(UUID.randomUUID());
    State currentState = State.init(rewriteId); // Cannot create branch from INIT
    when(stateRepository.get(rewriteId)).thenReturn(Optional.of(currentState));

    // Then
    assertThatThrownBy(() -> repositoryManager.createBranch(rewriteId, "new-branch")).isExactlyInstanceOf(
      InvalidStateTransitionException.class
    );
  }
}
