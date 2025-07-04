package fr.rewrite.server.shared.error.infrastructure.primary;

import fr.rewrite.server.shared.error.domain.AssertionErrorType;
import fr.rewrite.server.shared.error.domain.AssertionException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 1000)
class AssertionErrorsHandler {

  private static final Logger log = LoggerFactory.getLogger(AssertionErrorsHandler.class);
  private static final String MESSAGES_PREFIX = "assertion-error.";

  private final MessageSource messages;

  public AssertionErrorsHandler(@Qualifier("assertionErrorMessageSource") MessageSource messages) {
    Locale.setDefault(Locale.ENGLISH);

    this.messages = messages;
  }

  @ExceptionHandler(AssertionException.class)
  ProblemDetail handleAssertionError(AssertionException exception) {
    HttpStatus status = buildStatus(exception);
    var problem = ProblemDetail.forStatusAndDetail(status, buildDetail(exception));

    problem.setTitle(getMessage(exception.type(), "title"));
    problem.setProperty("key", exception.type().name());

    logException(exception, status);

    return problem;
  }

  private HttpStatus buildStatus(AssertionException exception) {
    return Stream.of(exception.getStackTrace())
      .map(StackTraceElement::getClassName)
      .filter(inApplication())
      .filter(notInErrorDomain())
      .filter(notInDomain())
      .findFirst()
      .filter(primaryClass())
      .map(className -> HttpStatus.BAD_REQUEST)
      .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private Predicate<String> inApplication() {
    return className -> className.startsWith("fr.rewrite.server.");
  }

  private Predicate<String> notInErrorDomain() {
    return className -> !className.startsWith("fr.rewrite.server.shared.error.domain");
  }

  private Predicate<String> notInDomain() {
    return className -> !className.startsWith("fr.rewrite.server.domain");
  }

  private Predicate<String> primaryClass() {
    return className -> className.contains(".primary.");
  }

  private String buildDetail(AssertionException exception) {
    String messageTemplate = getMessage(exception.type(), "detail");

    return ArgumentsReplacer.replaceParameters(messageTemplate, parameters(exception));
  }

  private Map<String, String> parameters(AssertionException exception) {
    HashMap<String, String> parameters = new HashMap<>(exception.parameters());
    parameters.put("field", exception.field());

    return parameters;
  }

  private String getMessage(AssertionErrorType type, String suffix) {
    return messages.getMessage(MESSAGES_PREFIX + type.name() + "." + suffix, null, locale());
  }

  private Locale locale() {
    return LocaleContextHolder.getLocale();
  }

  private void logException(AssertionException exception, HttpStatus status) {
    if (status.is4xxClientError()) {
      if (status.value() == 404 || status.value() == 400) {
        log.warn(exception.getMessage());
      } else {
        log.warn(exception.getMessage(), exception);
      }
    } else {
      log.error(exception.getMessage(), exception);
    }
  }
}
