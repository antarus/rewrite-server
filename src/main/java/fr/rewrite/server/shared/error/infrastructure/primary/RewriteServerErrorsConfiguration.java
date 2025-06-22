package fr.rewrite.server.shared.error.infrastructure.primary;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
class RewriteServerErrorsConfiguration {

  @Bean("applicationErrorMessageSource")
  MessageSource applicationErrorMessageSource() {
    var source = new ReloadableResourceBundleMessageSource();

    source.setBasename("classpath:/messages/errors/rewrite-server-errors-messages");
    source.setDefaultEncoding("UTF-8");

    return source;
  }
}
