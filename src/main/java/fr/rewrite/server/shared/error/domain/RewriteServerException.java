package fr.rewrite.server.shared.error.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RewriteServerException extends RuntimeException {

  private final ErrorKey key;
  private final ErrorStatus status;
  private final Map<String, String> parameters;

  protected RewriteServerException(RewriteServerExceptionBuilder builder) {
    super(buildMessage(builder), builder.cause);
    key = buildKey(builder);
    status = buildStatus(builder);
    parameters = Collections.unmodifiableMap(builder.parameters);
  }

  private static String buildMessage(RewriteServerExceptionBuilder builder) {
    Assert.notNull("builder", builder);

    if (builder.message == null) {
      return "An error occurred";
    }

    return builder.message;
  }

  private ErrorKey buildKey(RewriteServerExceptionBuilder builder) {
    if (builder.key == null) {
      return StandardErrorKey.INTERNAL_SERVER_ERROR;
    }

    return builder.key;
  }

  private ErrorStatus buildStatus(RewriteServerExceptionBuilder builder) {
    if (builder.status == null) {
      return defaultStatus();
    }

    return builder.status;
  }

  private ErrorStatus defaultStatus() {
    return Stream.of(Thread.currentThread().getStackTrace())
      .map(StackTraceElement::getClassName)
      .filter(inProject())
      .filter(notCurrentException())
      .findFirst()
      .filter(inPrimary())
      .map(className -> ErrorStatus.BAD_REQUEST)
      .orElse(ErrorStatus.INTERNAL_SERVER_ERROR);
  }

  private Predicate<String> inProject() {
    return className -> className.startsWith("fr.rewrite.server");
  }

  private Predicate<String> notCurrentException() {
    return className -> !className.contains(this.getClass().getName());
  }

  private Predicate<String> inPrimary() {
    return className -> className.contains(".primary");
  }

  public static RewriteServerExceptionBuilder internalServerError(ErrorKey key) {
    return builder(key).status(ErrorStatus.INTERNAL_SERVER_ERROR);
  }

  public static RewriteServerExceptionBuilder badRequest(ErrorKey key) {
    return builder(key).status(ErrorStatus.BAD_REQUEST);
  }

  public static RewriteServerException technicalError(String message) {
    return technicalError(message, null);
  }

  public static RewriteServerException technicalError(String message, Throwable cause) {
    return builder(StandardErrorKey.INTERNAL_SERVER_ERROR).message(message).cause(cause).build();
  }

  public static RewriteServerExceptionBuilder builder(ErrorKey key) {
    return new RewriteServerExceptionBuilder(key);
  }

  public ErrorKey key() {
    return key;
  }

  public ErrorStatus status() {
    return status;
  }

  public Map<String, String> parameters() {
    return parameters;
  }

  public static final class RewriteServerExceptionBuilder {

    private final ErrorKey key;
    private final Map<String, String> parameters = new HashMap<>();

    private String message;
    private Throwable cause;
    private ErrorStatus status;

    private RewriteServerExceptionBuilder(ErrorKey key) {
      this.key = key;
    }

    public RewriteServerExceptionBuilder message(String message) {
      this.message = message;

      return this;
    }

    public RewriteServerExceptionBuilder cause(Throwable cause) {
      this.cause = cause;

      return this;
    }

    public RewriteServerExceptionBuilder addParameters(Map<String, String> parameters) {
      Assert.notNull("parameters", parameters);

      parameters.forEach(this::addParameter);

      return this;
    }

    public RewriteServerExceptionBuilder addParameter(String key, String value) {
      Assert.notBlank("key", key);
      Assert.notNull("value", value);

      parameters.put(key, value);

      return this;
    }

    public RewriteServerExceptionBuilder status(ErrorStatus status) {
      this.status = status;

      return this;
    }

    public RewriteServerException build() {
      return new RewriteServerException(this);
    }
  }
}
