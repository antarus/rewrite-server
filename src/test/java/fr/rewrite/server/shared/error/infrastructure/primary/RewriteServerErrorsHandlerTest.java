package fr.rewrite.server.shared.error.infrastructure.primary;

import static org.mockito.Mockito.mock;

import ch.qos.logback.classic.Level;
import fr.rewrite.server.Logs;
import fr.rewrite.server.LogsSpy;
import fr.rewrite.server.LogsSpyExtension;
import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.log.LogPublisher;
import fr.rewrite.server.shared.error.domain.RewriteServerException;
import fr.rewrite.server.shared.error.domain.StandardErrorKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.MessageSource;

@UnitTest
@ExtendWith(LogsSpyExtension.class)
class RewriteServerErrorsHandlerTest {

  private static final RewriteServerErrorsHandler handler = new RewriteServerErrorsHandler(
    mock(LogPublisher.class),
    mock(MessageSource.class)
  );

  @Logs
  private LogsSpy logs;

  @Test
  void shouldLogServerErrorAsError() {
    handler.handleRewriteServerException(
      RewriteServerException.internalServerError(StandardErrorKey.INTERNAL_SERVER_ERROR).message("Oops").build()
    );

    logs.shouldHave(Level.ERROR, "Oops");
  }

  @Test
  void shouldLogClientErrorAsInfo() {
    handler.handleRewriteServerException(RewriteServerException.badRequest(StandardErrorKey.BAD_REQUEST).message("Oops").build());

    logs.shouldHave(Level.WARN, "Oops");
  }
}
