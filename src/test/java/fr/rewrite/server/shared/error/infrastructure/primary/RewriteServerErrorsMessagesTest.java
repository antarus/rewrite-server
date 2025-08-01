package fr.rewrite.server.shared.error.infrastructure.primary;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import fr.rewrite.server.shared.error.domain.ErrorKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

@UnitTest
class RewriteServerErrorsMessagesTest {

  private static final String BASE_PACKAGE = "fr.rewrite.server";

  private static final Set<Class<? extends ErrorKey>> ERRORS = new Reflections(
    new ConfigurationBuilder()
      .setUrls(ClasspathHelper.forPackage(BASE_PACKAGE))
      .setScanners(Scanners.SubTypes)
      .filterInputsBy(new FilterBuilder().includePackage(BASE_PACKAGE))
  ).getSubTypesOf(ErrorKey.class);

  private static final Map<String, Properties> ALL_ASSERTION_MESSAGES = loadMessages();

  private static Map<String, Properties> loadMessages() {
    try {
      return Files.list(Path.of("src/main/resources/messages/errors")).collect(
        Collectors.toUnmodifiableMap(Path::toString, toProperties())
      );
    } catch (IOException e) {
      throw new AssertionError();
    }
  }

  private static Function<Path, Properties> toProperties() {
    return file -> {
      var properties = new Properties();
      try {
        properties.load(Files.newInputStream(file));
      } catch (IOException e) {
        throw new AssertionError();
      }

      return properties;
    };
  }

  @Test
  void shouldHaveOnlyEnumImplementations() {
    ERRORS.forEach(error ->
      assertThat(error.isEnum() || error.isInterface())
        .as("Implementations of " + ErrorKey.class.getName() + " must be enums and " + error.getName() + " wasn't")
        .isTrue()
    );
  }

  @Test
  void shouldHaveFullappErrorTitleInAllSupportedLanguages() {
    errorKeys().map(error -> "error." + error.get() + ".title").forEach(assertHasMessage());
  }

  @Test
  void shouldHaveFullappErrorDetailInAllSupportedLanguages() {
    errorKeys().map(error -> "error." + error.get() + ".detail").forEach(assertHasMessage());
  }

  private Stream<ErrorKey> errorKeys() {
    return ERRORS.stream().filter(Class::isEnum).flatMap(error -> Stream.of(error.getEnumConstants()));
  }

  private Consumer<String> assertHasMessage() {
    return messageKey ->
      ALL_ASSERTION_MESSAGES.forEach((file, localeMessages) ->
        assertThat(localeMessages).as(() -> "Missing " + messageKey + " translation in " + file).containsKey(messageKey)
      );
  }
}
