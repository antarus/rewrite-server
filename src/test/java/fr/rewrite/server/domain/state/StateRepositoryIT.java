package fr.rewrite.server.domain.state;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.IntegrationTest;
import fr.rewrite.server.domain.RewriteId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@SpringBootTest
@Transactional
class StateRepositoryIT {

  @Autowired
  private StateRepository stateRepository;

  @Test
  void shouldSaveAndGetState() {
    RewriteId rewriteId = RewriteId.from(UUID.randomUUID());
    State state = State.init(rewriteId);

    stateRepository.save(state);

    Optional<State> foundState = stateRepository.get(rewriteId);

    assertThat(foundState).isPresent();
    assertThat(foundState.get().rewriteId()).isEqualTo(rewriteId);
  }

  @Test
  void shouldReturnEmptyForUnknownId() {
    RewriteId rewriteId = RewriteId.from(UUID.randomUUID());
    Optional<State> foundState = stateRepository.get(rewriteId);
    assertThat(foundState).isNotPresent();
  }

  @Test
  void shouldUpdateState() {
    // Given
    RewriteId rewriteId = RewriteId.from(UUID.randomUUID());
    State initialState = State.init(rewriteId);
    stateRepository.save(initialState);

    // When
    State stateToUpdate = stateRepository.get(rewriteId).orElseThrow();
    State updatedState = stateToUpdate.withStatus(StateEnum.CLONED);
    stateRepository.save(updatedState);

    // Then
    State foundState = stateRepository.get(rewriteId).orElseThrow();

    assertThat(foundState.status()).isEqualTo(StateEnum.CLONED);
    assertThat(foundState.updatedAt()).isAfter(initialState.updatedAt());
  }
}
