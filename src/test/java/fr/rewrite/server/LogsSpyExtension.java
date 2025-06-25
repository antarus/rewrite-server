package fr.rewrite.server;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.ModifierSupport;

public final class LogsSpyExtension
  implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver, TestInstancePostProcessor {

  private final LogsSpy logsSpy = new LogsSpy();

  @Override
  public void beforeEach(ExtensionContext context) {
    logsSpy.prepare();
  }

  @Override
  public void afterEach(ExtensionContext context) {
    logsSpy.reset();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
    throws ParameterResolutionException {
    return parameterContext.getParameter().getType().equals(LogsSpy.class);
  }

  @Override
  public LogsSpy resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
    throws ParameterResolutionException {
    return logsSpy;
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    injectFields(testClass, null, ModifierSupport::isStatic);
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    injectFields(testClass, testInstance, ModifierSupport::isNotStatic);
  }

  private void injectFields(Class<?> testClass, Object testInstance, Predicate<Field> predicate) {
    predicate = predicate.and(field -> LogsSpy.class.isAssignableFrom(field.getType()));
    findAnnotatedFields(testClass, Logs.class, predicate).forEach(field -> {
      try {
        field.setAccessible(true);
        field.set(testInstance, logsSpy);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    });
  }
}
