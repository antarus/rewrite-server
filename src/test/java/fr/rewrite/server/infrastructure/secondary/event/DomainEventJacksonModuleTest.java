package fr.rewrite.server.infrastructure.secondary.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.domain.events.LoggingEvent;
import fr.rewrite.server.domain.events.RepositoryCreatedEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    RepositoryCreatedEvent event = RepositoryCreatedEvent.from("my-new-repo");

    String json = objectMapper.writeValueAsString(event);

    System.out.println("Serialized RepositoryCreatedEvent:\n" + json);

    assertThat(json).contains("\"@class\" : \"" + RepositoryCreatedEvent.class.getName() + "\"");
    assertThat(json).contains("\"path\" : \"my-new-repo\"");
    assertThat(json).contains("eventId");
    assertThat(json).contains("occurredOn");
  }

  @Test
  @DisplayName("Serialization should include @class property for LoggingEvent")
  void serialization_shouldIncludeTypeInfoForLoggingEvent() throws JsonProcessingException {
    LoggingEvent event = LoggingEvent.from("Application started.");

    String json = objectMapper.writeValueAsString(event);

    System.out.println("Serialized LoggingEvent:\n" + json);

    assertThat(json).contains("\"@class\" : \"" + LoggingEvent.class.getName() + "\"");
    assertThat(json).contains("\"log\" : \"Application started.\"");
  }

  @Test
  @DisplayName("Deserialization should correctly resolve RepositoryCreatedEvent by @class property")
  void deserialization_shouldResolveRepositoryCreatedEvent() throws IOException {
    String json =
      "{\n" +
      "  \"@class\" : \"" +
      RepositoryCreatedEvent.class.getName() +
      "\",\n" +
      "  \"eventId\" : \"test-repo-id-123\",\n" +
      "  \"occurredOn\" : \"" +
      LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
      "\",\n" +
      "  \"path\" : \"deserialized-repo\"\n" +
      "}";

    DomainEvent deserializedEvent = objectMapper.readValue(json, DomainEvent.class);

    assertNotNull(deserializedEvent);
    assertThat(deserializedEvent).isInstanceOf(RepositoryCreatedEvent.class);
    RepositoryCreatedEvent repoEvent = (RepositoryCreatedEvent) deserializedEvent;

    assertThat(repoEvent.eventId()).isEqualTo("test-repo-id-123");
    assertThat(repoEvent.occurredOn().toLocalDate()).isEqualTo(LocalDateTime.now().toLocalDate());
  }

  @Test
  @DisplayName("Deserialization should correctly resolve LoggingEvent by @class property")
  void deserialization_shouldResolveLoggingEvent() throws IOException {
    String json =
      "{\n" +
      "  \"@class\": \"" +
      LoggingEvent.class.getName() +
      "\",\n" +
      "  \"eventId\": \"test-log-id-456\",\n" +
      "  \"occurredOn\": \"" +
      LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
      "\",\n" +
      "  \"log\": \"deserialized log message\"\n" +
      "}";

    DomainEvent deserializedEvent = objectMapper.readValue(json, DomainEvent.class);

    assertNotNull(deserializedEvent);
    assertThat(deserializedEvent).isInstanceOf(LoggingEvent.class);
    LoggingEvent logEvent = (LoggingEvent) deserializedEvent;

    assertThat(logEvent.eventId()).isEqualTo("test-log-id-456");
  }

  @Test
  @DisplayName("Deserialization should fail for unknown subtype not in @JsonSubTypes")
  void deserialization_shouldFailForUnknownSubtype() {
    String unknownEventClassName = "fr.rewrite.server.domain.events.UnknownExistEvent";
    String json =
      "{\n" +
      "  \"@class\": \"" +
      unknownEventClassName +
      "\",\n" +
      "  \"eventId\": \"unknown-id\",\n" +
      "  \"occurredOn\": \"" +
      LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
      "\",\n" +
      "  \"data\": \"some data\"\n" +
      "}";
    assertThrows(JsonMappingException.class, () -> objectMapper.readValue(json, DomainEvent.class));
  }
}
