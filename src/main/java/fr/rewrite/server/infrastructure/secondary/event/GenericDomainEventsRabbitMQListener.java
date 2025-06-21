package fr.rewrite.server.infrastructure.secondary.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.domain.events.DomainEventHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

public class GenericDomainEventsRabbitMQListener {

  private static final Logger log = LoggerFactory.getLogger(GenericDomainEventsRabbitMQListener.class);
  private final ObjectMapper objectMapper;
  private final DomainEventHandlerService domainEventHandlerService;

  public GenericDomainEventsRabbitMQListener(
    @Qualifier("objectMapperEvent") ObjectMapper objectMapper,
    DomainEventHandlerService domainEventHandlerService
  ) {
    this.objectMapper = objectMapper;
    this.domainEventHandlerService = domainEventHandlerService;
  }

  @Bean
  Queue allDomainEventsQueue() {
    return new Queue("all.domain.events.notification.queue", true); // Durable
  }

  @Bean
  Binding bindAllDomainEventsQueue(Queue allDomainEventsQueue, TopicExchange domainEventsExchange) {
    return BindingBuilder.bind(allDomainEventsQueue).to(domainEventsExchange).with("#");
  }

  @RabbitListener(queues = "all.domain.events.notification.queue")
  public void receiveAllDomainEvents(String jsonMessage) {
    try {
      DomainEvent event = objectMapper.readValue(jsonMessage, DomainEvent.class);

      log.debug("GenericDomainEventsRabbitMQListener: Received event of type {} from RabbitMQ.", event.getClass().getSimpleName());
      domainEventHandlerService.handleEvent(event); // Délègue au service de gestion d'événements du domaine
    } catch (Exception e) {
      log.error("Error processing RabbitMQ message for generic event: {}", e.getMessage());
      //TODO
    }
  }
}
