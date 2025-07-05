package fr.rewrite.server.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.shared.error.domain.NumberValueTooLowException;
import org.junit.jupiter.api.Test;

@UnitTest
class SequenceIdTest {

  @Test
  void shouldNotBuildWithNegativeValue() {
    assertThatThrownBy(() -> new SequenceId(-1)).isExactlyInstanceOf(NumberValueTooLowException.class).hasMessageContaining("value");
  }

  @Test
  void shouldBuildWithPositiveValue() {
    assertThat(new SequenceId(1).value()).isEqualTo(1);
  }

  @Test
  void shouldGetNextValue() {
    assertThat(new SequenceId(1).next()).isEqualTo(new SequenceId(11));
  }

  @Test
  void shouldCompare() {
    assertThat(new SequenceId(1).compareTo(new SequenceId(2))).isNegative();
    assertThat(new SequenceId(2).compareTo(new SequenceId(1))).isPositive();
    assertThat(new SequenceId(1).compareTo(new SequenceId(1))).isZero();
  }

  @Test
  void shouldHaveInitialValue() {
    assertThat(SequenceId.INITIAL).isEqualTo(new SequenceId(0));
  }
}
