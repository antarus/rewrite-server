package fr.rewrite.server.domain.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@UnitTest
class StateTest {

  @Test
  void shouldInitState() {
    RewriteId rewriteId = RewriteId.from(UUID.randomUUID());
    Instant before = Instant.now();
    State state = State.init(rewriteId);
    Instant after = Instant.now();

    assertThat(state.rewriteId()).isEqualTo(rewriteId);
    assertThat(state.status()).isEqualTo(StateEnum.INIT);
    assertThat(state.createdAt()).isBetween(before, after);
    assertThat(state.updatedAt()).isEqualTo(state.createdAt());
  }

  @Test
  void shouldChangeStatus() {
    State initialState = State.init(RewriteId.from(UUID.randomUUID()));
    Instant initialUpdatedAt = initialState.updatedAt();

    // To ensure updatedAt is different
    try {
      Thread.sleep(1);
    } catch (InterruptedException e) {
      // Should not happen
    }

    State updatedState = initialState.withStatus(StateEnum.CLONING);

    assertThat(updatedState.rewriteId()).isEqualTo(initialState.rewriteId());
    assertThat(updatedState.status()).isEqualTo(StateEnum.CLONING);
    assertThat(updatedState.createdAt()).isEqualTo(initialState.createdAt());
    assertThat(updatedState.updatedAt()).isAfter(initialUpdatedAt);
  }

  @Test
  void shouldNotCreateStateWithoutRewriteId() {
    assertThatThrownBy(() -> new State(null, StateEnum.INIT, Instant.now(), Instant.now()))
      .isExactlyInstanceOf(MissingMandatoryValueException.class)
      .hasMessageContaining("rewriteId");
  }

  @Test
  void shouldNotCreateStateWithFutureDate() {
    RewriteId rewriteId = RewriteId.from(UUID.randomUUID());
    Instant now = Instant.now();
    Instant future = now.plusSeconds(10);

    assertThatThrownBy(() -> new State(rewriteId, StateEnum.INIT, future, now)).hasMessageContaining("createdAt");

    assertThatThrownBy(() -> new State(rewriteId, StateEnum.INIT, now, future)).hasMessageContaining("updatedAt");
  }
}
