package fr.rewrite.server.wire.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.rewrite.server.domain.events.DomainEventHandlerService;
import fr.rewrite.server.domain.events.EventBusPort;
import fr.rewrite.server.domain.state.StateRepository;
import fr.rewrite.server.infrastructure.secondary.event.GenericDomainEventsRabbitMQListener;
import fr.rewrite.server.infrastructure.secondary.event.RabbitMQEventBusAdapter;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("rabbit")
class RabbitMQConfiguration {

  @Bean
  public TopicExchange domainEventsExchange() {
    // Le nom de l'échange doit correspondre à celui utilisé dans l'adaptateur de publication
    return new TopicExchange("domain.events.exchange");
  }

  @Bean
  public RabbitTemplate rabbitTemplate(
    org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
    @Qualifier("objectMapperEvent") ObjectMapper objectMapperRabbit
  ) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter(objectMapperRabbit));
    return rabbitTemplate;
  }

  @Bean
  public DomainEventHandlerService domainEventHandlerService(StateRepository stateRepository) {
    return new DomainEventHandlerService(stateRepository);
  }

  @Bean
  public EventBusPort rabbitMQEventBusAdapter(RabbitTemplate rabbitTemplate, TopicExchange topicExchange) {
    return new RabbitMQEventBusAdapter(rabbitTemplate, topicExchange);
  }

  @Bean
  public GenericDomainEventsRabbitMQListener genericDomainEventsRabbitMQListener(
    @Qualifier("objectMapperEvent") ObjectMapper objectMapper,
    DomainEventHandlerService domainEventHandlerService
  ) {
    return new GenericDomainEventsRabbitMQListener(objectMapper, domainEventHandlerService);
  }
  // Implémentation du port UserRepositoryPort (simulée pour la démo)
  //    @Bean
  //    public UserRepositoryPort userRepositoryPort() {
  //        return user -> System.out.println("Saving user: " + user.getUsername());
  //    }
  //
  //    // Implémentation du port EmailSenderPort (simulée pour la démo)
  //    @Bean
  //    public EmailSenderPort emailSenderPort() {
  //        return (username, userId) -> System.out.println("Simulating sending welcome email to " + username);
  //    }
  //
  //    // Implémentation du service d'analyse utilisateur (simulée pour la démo)
  //    @Bean
  //    public UserAnalyticsService userAnalyticsService() {
  //        return userId -> System.out.println("Simulating recording new user analytics for user ID: " + userId);
  //    }
  //
  //    // Implémentation du port RepositoryNotificationService (simulée pour la démo)
  //    @Bean
  //    public RepositoryNotificationService repositoryNotificationService() {
  //        return (repoName, ownerId) -> System.out.println("Simulating sending notification for repository " + repoName + " by " + ownerId);
  //    }

}
