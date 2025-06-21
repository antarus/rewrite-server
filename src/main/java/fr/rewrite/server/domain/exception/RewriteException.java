package fr.rewrite.server.domain.exception;

import fr.rewrite.server.shared.error.domain.Assert;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RewriteException extends RuntimeException {

  private final String key;
  private final ErrorStatus status;
  private final Map<String, String> parameters;

  protected RewriteException(RewriteExceptionBuilder builder) {
    super(buildMessage(builder), builder.cause);
    key = buildKey(builder);
    status = buildStatus(builder);
    parameters = Collections.unmodifiableMap(builder.parameters);
  }

  private static String buildMessage(RewriteExceptionBuilder builder) {
    Assert.notNull("builder", builder);

    if (builder.message == null) {
      return "An error occurred";
    }

    return builder.message;
  }

  private String buildKey(RewriteExceptionBuilder builder) {
    if (builder.key == null) {
      return StandardErrorKey.INTERNAL_SERVER_ERROR.get();
    }

    return builder.key;
  }

  private ErrorStatus buildStatus(RewriteExceptionBuilder builder) {
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

  public static RewriteExceptionBuilder badRequestBuilder(String message) {
    return builder(StandardErrorKey.BAD_REQUEST.get()).status(ErrorStatus.BAD_REQUEST).message(message);
  }

  public static RewriteException badRequest(String message) {
    return builder(StandardErrorKey.BAD_REQUEST.get()).status(ErrorStatus.BAD_REQUEST).message(message).build();
  }

  public static RewriteExceptionBuilder technicalErrorBuilder(String message) {
    return builder(StandardErrorKey.INTERNAL_SERVER_ERROR.get()).message(message);
  }

  public static RewriteException technicalError(String message) {
    return technicalError(message, null);
  }

  public static RewriteException technicalError(String message, Throwable cause) {
    return builder(StandardErrorKey.INTERNAL_SERVER_ERROR.get()).message(message).cause(cause).build();
  }

  public static RewriteExceptionBuilder builder(String key) {
    return new RewriteExceptionBuilder(key);
  }

  public String key() {
    return key;
  }

  public ErrorStatus status() {
    return status;
  }

  public Map<String, String> parameters() {
    return parameters;
  }

  public static final class RewriteExceptionBuilder {

    private final String key;
    private final Map<String, String> parameters = new HashMap<>();

    private String message;
    private Throwable cause;
    private ErrorStatus status;

    private RewriteExceptionBuilder(String key) {
      this.key = key;
    }

    public RewriteExceptionBuilder message(String message) {
      this.message = message;

      return this;
    }

    public RewriteExceptionBuilder cause(Throwable cause) {
      this.cause = cause;

      return this;
    }

    public RewriteExceptionBuilder addParameters(Map<String, String> parameters) {
      Assert.notNull("parameters", parameters);

      parameters.forEach(this::addParameter);

      return this;
    }

    public RewriteExceptionBuilder addParameter(String key, String value) {
      Assert.notBlank("key", key);
      Assert.notNull("value", value);

      parameters.put(key, value);

      return this;
    }

    public RewriteExceptionBuilder status(ErrorStatus status) {
      this.status = status;

      return this;
    }

    public RewriteException build() {
      return new RewriteException(this);
    }
  }
}
