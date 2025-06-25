package fr.rewrite.server.domain.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.shared.error.domain.MissingMandatoryValueException;
import java.io.IOException;
import org.junit.jupiter.api.Test;

@UnitTest
class RepositoryClonedEventTest {

  @Test
  void shoudlTrowExceptionIfRewridIsNull() throws IOException {
    assertThatThrownBy(() -> RepositoryClonedEvent.from(null)).isInstanceOf(MissingMandatoryValueException.class);
  }

  @Test
  void shoudlTrowExceptionIfRewridIsNull2() throws IOException {
    assertThatThrownBy(() ->
      RepositoryClonedEvent.RepositoryClonedEventBuilder.aRepositoryClonedEvent().rewriteId((RewriteId) null).build()
    ).isInstanceOf(MissingMandatoryValueException.class);
  }
}
