package fr.rewrite.server.shared.error.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import fr.rewrite.server.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class StringTooShortExceptionTest {

  @Test
  void shouldGetExceptionInformation() {
    StringTooShortException exception = StringTooShortException.builder().field("myField").minLength(10).value("value").build();

    assertThat(exception.type()).isEqualTo(AssertionErrorType.STRING_TOO_SHORT);
    assertThat(exception.field()).isEqualTo("myField");
    assertThat(exception.parameters()).containsOnly(entry("minLength", "10"), entry("currentLength", "5"));
    assertThat(exception.getMessage()).isEqualTo("The value \"value\" in field \"myField\" must be at least 10 long but was only 5");
  }
}
