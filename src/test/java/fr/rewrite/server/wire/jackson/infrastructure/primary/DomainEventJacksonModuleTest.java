package fr.rewrite.server.wire.jackson.infrastructure.primary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.rewrite.server.UnitTest;
import fr.rewrite.server.domain.RewriteId;
import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.domain.events.LoggingEvent;
import fr.rewrite.server.domain.repository.RepositoryClonedEvent;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest
class DomainEventJacksonModuleTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new DomainEventJacksonModule());
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.findAndRegisterModules();
  }

  @Test
  @DisplayName("Serialization should include @class property for RepositoryCreatedEvent")
  void serialization_shouldIncludeTypeInfoForRepositoryCreatedEvent() throws JsonProcessingException {
    RepositoryClonedEvent event = RepositoryClonedEvent.from(RewriteId.from(UUID.fromString("72995000-df51-4fc8-ad40-28f29a1bf54d")));

    String json = objectMapper.writeValueAsString(event);

    System.out.println("Serialized RepositoryCreatedEvent:\n" + json);

    assertThat(json).contains("\"@type\" : \"" + RepositoryClonedEvent.class.getSimpleName() + "\"");
    assertThat(json).contains("\"rewriteId\" : \"72995000-df51-4fc8-ad40-28f29a1bf54d\"");
    assertThat(json).contains("eventId");
    assertThat(json).contains("occurredOn");
  }

  @Test
  @DisplayName("Serialization should include @class property for LoggingEvent")
  void serialization_shouldIncludeTypeInfoForLoggingEvent() throws JsonProcessingException {
    LoggingEvent event = LoggingEvent.from("Application started.");

    String json = objectMapper.writeValueAsString(event);

    System.out.println("Serialized LoggingEvent:\n" + json);

    assertThat(json).contains("\"@type\" : \"" + LoggingEvent.class.getSimpleName() + "\"");
    assertThat(json).contains("\"log\" : \"Application started.\"");
  }

  @Test
  @DisplayName("Deserialization should correctly resolve RepositoryCreatedEvent by @class property")
  void deserialization_shouldResolveRepositoryCreatedEvent() throws IOException {
    String json =
      """
      {
        "@type" : "%s",
        "eventId" : "636e6725-1f9c-41cb-833f-b499326fa364",
        "occurredOn" : "%s",
        "eventId" : "636e6725-1f9c-41cb-833f-b499326fa361"
      }
      """.formatted(RepositoryClonedEvent.class.getSimpleName(), Instant.now());

    DomainEvent deserializedEvent = objectMapper.readValue(json, DomainEvent.class);

    assertNotNull(deserializedEvent);
    assertThat(deserializedEvent).isInstanceOf(RepositoryClonedEvent.class);
    RepositoryClonedEvent repoEvent = (RepositoryClonedEvent) deserializedEvent;

    assertThat(repoEvent.eventId()).isEqualTo(UUID.fromString("636e6725-1f9c-41cb-833f-b499326fa361"));
    assertThat(repoEvent.occurredOn()).isBeforeOrEqualTo(Instant.now());
  }

  @Test
  @DisplayName("Deserialization should correctly resolve LoggingEvent by @class property")
  void deserialization_shouldResolveLoggingEvent() throws IOException {
    String json =
      """
      {
        "@type": "%s",
        "eventId": "af028d76-0330-46d8-969a-4f57346e104e",
        "occurredOn": "%s",
        "log": "deserialized log message"
      }
      """.formatted(LoggingEvent.class.getSimpleName(), Instant.now().minusSeconds(10));
    DomainEvent deserializedEvent = objectMapper.readValue(json, DomainEvent.class);

    assertNotNull(deserializedEvent);
    assertThat(deserializedEvent).isInstanceOf(LoggingEvent.class);
    LoggingEvent logEvent = (LoggingEvent) deserializedEvent;

    assertThat(logEvent.eventId()).isEqualTo(UUID.fromString("af028d76-0330-46d8-969a-4f57346e104e"));
  }

  @Test
  @DisplayName("Deserialization should fail for unknown subtype not in @JsonSubTypes")
  void deserialization_shouldFailForUnknownSubtype() {
    String unknownEventClassName = "UnknownExistEvent";
    String json =
      """
      {
        "@type": "%s",
        "eventId": "unknown-id",
        "occurredOn": "%s",
        "data": "some data"
      }
      """.formatted(unknownEventClassName, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    assertThrows(JsonMappingException.class, () -> objectMapper.readValue(json, DomainEvent.class));
  }
}
