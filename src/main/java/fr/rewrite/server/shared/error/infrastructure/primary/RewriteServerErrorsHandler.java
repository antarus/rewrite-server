package fr.rewrite.server.shared.error.infrastructure.primary;

import fr.rewrite.server.shared.enumeration.domain.Enums;
import fr.rewrite.server.shared.error.domain.ErrorKey;
import fr.rewrite.server.shared.error.domain.RewriteServerException;
import java.util.Locale;
import java.util.Optional;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 999)
class RewriteServerErrorsHandler {

  private static final Logger log = LoggerFactory.getLogger(RewriteServerErrorsHandler.class);
  private static final String MESSAGES_PREFIX = "error.";

  private final MessageSource messages;

  @Value("${developer.mode}")
  private boolean developerMode = false;

  public RewriteServerErrorsHandler(@Qualifier("applicationErrorMessageSource") MessageSource messages) {
    Locale.setDefault(Locale.ENGLISH);

    this.messages = messages;
  }

  @ExceptionHandler(RewriteServerException.class)
  ProblemDetail handleRewriteServerException(RewriteServerException exception) {
    HttpStatus status = Optional.ofNullable(Enums.map(exception.status(), HttpStatus.class)).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    String message = buildDetail(exception);
    var problem = ProblemDetail.forStatusAndDetail(status, message);

    problem.setTitle(getMessage(exception.key(), "title"));
    problem.setProperty("key", exception.key().get());

    logException(message, exception, status);

    return problem;
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail handleException(Exception exception) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    String message = exception.getMessage();
    var problem = ProblemDetail.forStatusAndDetail(status, message);

    logException(message, exception, status);

    return problem;
  }

  private String buildDetail(RewriteServerException exception) {
    String messageTemplate = "";
    try {
      messageTemplate = getMessage(exception.key(), "detail");
      if (messageTemplate.equals("message-not-found")) {
        messageTemplate = exception.getMessage();
      }
    } catch (Exception e) {
      messageTemplate = exception.getMessage();
    }
    messageTemplate = ArgumentsReplacer.replaceParameters(messageTemplate, exception.parameters());
    if (developerMode) {
      String rootCause = ExceptionUtils.getRootCause(exception).getMessage();
      if (rootCause != null && !rootCause.equals(messageTemplate)) {
        messageTemplate = messageTemplate + ", ROOT : " + ExceptionUtils.getRootCause(exception).getMessage();
      }
    }
    return messageTemplate;
  }

  private String getMessage(ErrorKey key, String suffix) {
    return messages.getMessage(MESSAGES_PREFIX + key.get() + "." + suffix, null, locale());
  }

  private Locale locale() {
    return LocaleContextHolder.getLocale();
  }

  private void logException(String message, Exception exception, HttpStatus status) {
    if (status.is4xxClientError()) {
      if (status.value() == 404 || status.value() == 400) {
        log.warn(message);
      } else {
        log.warn(message, exception);
      }
    } else {
      log.error(message, exception);
    }
  }
}
