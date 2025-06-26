package fr.rewrite.server.wire.jackson.infrastructure.primary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.events.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UnitTest
class DomainEventMixInTest {

  private static final Logger log = LoggerFactory.getLogger(DomainEventMixInTest.class);
  private static final String DOMAIN_EVENTS_PACKAGE = "fr.rewrite.server.domain";

  @Test
  @DisplayName("All concrete DomainEvent implementations should be registered in DomainEventMixIn")
  void allDomainEventsShouldBeRegisteredInDomainEventMixIn() {
    Reflections reflections = new Reflections(
      new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(DOMAIN_EVENTS_PACKAGE)).setScanners(Scanners.SubTypes)
    );
    Set<Class<? extends DomainEvent>> actualDomainEventClasses = reflections.getSubTypesOf(DomainEvent.class);

    actualDomainEventClasses.remove(DomainEvent.class);

    Set<Class<? extends DomainEvent>> ignoredTestEvents = new HashSet<>();
    ignoredTestEvents.add(TestEvent.class);
    ignoredTestEvents.add(AnotherTestEvent.class);
    ignoredTestEvents.add(UnknownEvent.class);
    ignoredTestEvents.add(LoggingEvent.class);

    actualDomainEventClasses.removeAll(ignoredTestEvents);

    JsonSubTypes jsonSubTypes = DomainEventMixIn.class.getAnnotation(JsonSubTypes.class);

    if (jsonSubTypes == null) {
      fail("DomainEventMixIn must have @JsonSubTypes annotation.");
    }

    Set<Class<?>> registeredMixInClasses = Arrays.stream(jsonSubTypes.value()).map(JsonSubTypes.Type::value).collect(Collectors.toSet());

    Set<Class<?>> missingInMixIn = actualDomainEventClasses
      .stream()
      .filter(eventClass -> !registeredMixInClasses.contains(eventClass))
      .collect(Collectors.toSet());

    assertThat(missingInMixIn)
      .withFailMessage(
        "The following production DomainEvent classes are present in package '%s' but are NOT registered in DomainEventMixIn: %s",
        DOMAIN_EVENTS_PACKAGE,
        missingInMixIn.stream().map(Class::getName).collect(Collectors.joining(", "))
      )
      .isEmpty();

    Set<Class<?>> extraneousInMixIn = registeredMixInClasses
      .stream()
      .filter(registeredClass -> !actualDomainEventClasses.contains(registeredClass) && DomainEvent.class.isAssignableFrom(registeredClass))
      .collect(Collectors.toSet());

    assertThat(extraneousInMixIn)
      .withFailMessage(
        "The following classes are registered in DomainEventMixIn but are NOT found among expected production DomainEvent classes in package '%s' or do not implement DomainEvent: %s",
        DOMAIN_EVENTS_PACKAGE,
        extraneousInMixIn.stream().map(Class::getName).collect(Collectors.joining(", "))
      )
      .isEmpty();

    log.info(
      "All production DomainEvent classes in package '{}' are correctly registered in DomainEventMixIn. " +
      "Found {} production DomainEvents and {} registered types (including potential test events).",
      DOMAIN_EVENTS_PACKAGE,
      actualDomainEventClasses.size(),
      registeredMixInClasses.size()
    );
  }
}
