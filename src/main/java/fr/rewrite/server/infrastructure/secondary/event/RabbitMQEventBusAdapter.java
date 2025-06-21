package fr.rewrite.server.infrastructure.secondary.event;

import fr.rewrite.server.domain.events.DomainEvent;
import fr.rewrite.server.domain.spi.EventBusPort;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitMQEventBusAdapter implements EventBusPort {

  private static final Logger log = LoggerFactory.getLogger(RabbitMQEventBusAdapter.class);

  private final RabbitTemplate rabbitTemplate;
  private final TopicExchange topicExchange;

  public RabbitMQEventBusAdapter(RabbitTemplate rabbitTemplate, TopicExchange topicExchange) {
    this.rabbitTemplate = rabbitTemplate;
    this.topicExchange = topicExchange;
  }

  @Override
  public void publish(DomainEvent event) {
    String routingKey = determineRoutingKey(event);

    if (routingKey == null || routingKey.isEmpty()) {
      log.error("Warning: No routing key determined for event type: {}", event.getClass().getSimpleName());
      return;
    }

    try {
      rabbitTemplate.convertAndSend(topicExchange.getName(), routingKey, event);

      log.debug(
        "Published event to RabbitMQ: {} (ID: {}) with routing key '{}' on exchange '{}'",
        event.getClass().getSimpleName(),
        event.eventId(),
        routingKey,
        topicExchange.getName()
      );
    } catch (Exception e) {
      log.error("Failed to publish event {} of type {} to RabbitMQ: {}", event.eventId(), event.getClass().getSimpleName(), e.getMessage());
      //TODO
    }
  }

  private String determineRoutingKey(DomainEvent event) {
    String className = event.getClass().getSimpleName();
    if (className.endsWith("Event")) {
      return className.substring(0, className.length() - "Event".length()).toLowerCase() + ".created";
    }
    return className.toLowerCase();
  }

  @Override
  public <T extends DomainEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
    log.warn("RabbitMQEventBusAdapter: Direct 'subscribe' method is not used for external message brokers like RabbitMQ.");
    log.warn("Consumers should use @RabbitListener (Spring AMQP) or similar mechanisms to listen to specific queues/topics.");
    // Cette implémentation peut rester vide ou lancer une UnsupportedOperationException
    //       throw new UnsupportedOperationException("Direct 'subscribe' method is not used for external message brokers like RabbitMQ");
  }

  @Override
  public void subscribeAll(Consumer<DomainEvent> handler) {
    log.warn("RabbitMQEventBusAdapter: Direct 'subscribeAll' method is not used for external message brokers like RabbitMQ.");
    log.warn("Use a generic @RabbitListener bound to a wildcard routing key (e.g., '#') on a dedicated queue.");
    //        throw new UnsupportedOperationException("Direct 'subscribeAll' method is not used for external message brokers like RabbitMQ");
  }
}
