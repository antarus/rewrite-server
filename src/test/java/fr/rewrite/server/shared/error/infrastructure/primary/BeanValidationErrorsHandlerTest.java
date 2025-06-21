package fr.rewrite.server.shared.error.infrastructure.primary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class BeanValidationErrorsHandlerTest {

  private BeanValidationErrorsHandler handler;
  private ListAppender<ILoggingEvent> listAppender;
  private Logger testLogger;

  @BeforeEach
  void setUp() {
    handler = new BeanValidationErrorsHandler();

    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    testLogger = loggerContext.getLogger(BeanValidationErrorsHandler.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    testLogger.addAppender(listAppender);
    testLogger.setLevel(Level.INFO);
    testLogger.setAdditive(true);
  }

  @AfterEach
  void tearDown() {
    testLogger.detachAppender(listAppender);
    listAppender.stop();
    listAppender.list.clear();
  }

  @Test
  @DisplayName("Should handle MethodArgumentNotValidException and log info")
  void shouldHandleMethodArgumentNotValidAndLogInfo() throws NoSuchMethodException {
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError(
      "myObject",
      "myField",
      "rejectedValue",
      false,
      new String[] { "NotNull.myField" },
      new Object[] {},
      "must not be null"
    );

    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
    when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

    MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
      new MethodParameter(BeanValidationErrorsHandlerTest.class.getMethod("failingMethod"), -1),
      bindingResult
    );

    ProblemDetail problem = handler.handleMethodArgumentNotValid(exception);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getTitle()).isEqualTo("Bean validation error");
    assertThat(problem.getDetail()).isEqualTo("One or more fields were invalid. See 'errors' for details.");
    assertThat(problem.getProperties()).containsKey("errors");
    assertThat((java.util.Map<String, String>) problem.getProperties().get("errors")).containsEntry("myField", "must not be null");

    assertThat(listAppender.list).anyMatch(
      event ->
        event.getLevel() == Level.INFO &&
        event
          .getFormattedMessage()
          .contains(
            "Validation failed for argument [-1] in public void fr.rewrite.server.shared.error.infrastructure.primary.BeanValidationErrorsHandlerTest.failingMethod()"
          )
    );

    assertThat(listAppender.list).anyMatch(
      event -> event.getLevel() == Level.INFO && event.getFormattedMessage().contains("must not be null")
    );
    assertThat(listAppender.list).anyMatch(event -> event.getLevel() == Level.INFO && event.getFormattedMessage().contains("myField"));
  }

  public void failingMethod() {
    // empty method
  }

  @Test
  @DisplayName("Should handle ConstraintViolationException and log info")
  void shouldHandleConstraintViolationAndLogInfo() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    ValidatedBean bean = new ValidatedBean();

    Set<ConstraintViolation<ValidatedBean>> violations = validator.validate(bean);
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    ProblemDetail problem = handler.handleConstraintViolationException(exception);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getTitle()).isEqualTo("Bean validation error");
    assertThat(problem.getDetail()).isEqualTo("One or more fields were invalid. See 'errors' for details.");
    assertThat(problem.getProperties()).containsKey("errors");
    assertThat((java.util.Map<String, String>) problem.getProperties().get("errors")).containsEntry("parameter", "must not be null");

    //    assertThat(listAppender.list).anyMatch(event ->
    //            event.getLevel() == Level.INFO &&
    //                    event.getFormattedMessage().contains("Constraint violations found")
    //    );
    assertThat(listAppender.list).anyMatch(
      event -> event.getLevel() == Level.INFO && event.getFormattedMessage().contains("parameter: must not be null")
    );
  }

  static class ValidatedBean {

    @NotNull(message = "must not be null")
    private String parameter;
  }
}
