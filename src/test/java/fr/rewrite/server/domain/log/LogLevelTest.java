package fr.rewrite.server.domain.log;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class LogLevelTest {

  @Test
  void shouldHaveAllExpectedLevels() {
    assertThat(LogLevel.values()).containsExactlyInAnyOrder(LogLevel.TRACE, LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR);
  }

  @Test
  void traceLevelShouldExist() {
    assertThat(LogLevel.TRACE).isNotNull();
  }

  @Test
  void debugLevelShouldExist() {
    assertThat(LogLevel.DEBUG).isNotNull();
  }

  @Test
  void infoLevelShouldExist() {
    assertThat(LogLevel.INFO).isNotNull();
  }

  @Test
  void warnLevelShouldExist() {
    assertThat(LogLevel.WARN).isNotNull();
  }

  @Test
  void errorLevelShouldExist() {
    assertThat(LogLevel.ERROR).isNotNull();
  }
}
