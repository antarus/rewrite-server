package fr.rewrite.server.wire.jackson.infrastructure.primary;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.rewrite.server.domain.datastore.DatastoreSavable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
class JacksonConfiguration {

  @Bean
  Jdk8Module jdk8Module() {
    return new Jdk8Module();
  }

  @Bean("objectMapperDs")
  public ObjectMapper objectMapperDatastore(Jackson2ObjectMapperBuilder builder) {
    ObjectMapper mapper = builder
      .createXmlMapper(false)
      .build()
      .findAndRegisterModules()
      .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(new Jdk8Module());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
      .allowIfBaseType("java.util.Set")
      .allowIfBaseType("java.util.List")
      .allowIfBaseType("java.util.Map")
      .allowIfSubTypeIsArray()
      .build();
    mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, JsonTypeInfo.As.PROPERTY);
    mapper.addMixIn(DatastoreSavable.class, DomainDatastoreSavableMixIn.class);
    return mapper;
  }

  @Bean("objectMapperEvent")
  public ObjectMapper objectMapperEvent(Jackson2ObjectMapperBuilder builder) {
    ObjectMapper mapper = builder
      .createXmlMapper(false)
      .build()
      .findAndRegisterModules()
      .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    mapper.registerModule(new JavaTimeModule());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().allowIfBaseType("fr.rewrite.server.domain.events").build();

    mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);

    return mapper;
  }
}
