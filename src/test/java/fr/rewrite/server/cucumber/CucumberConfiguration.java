package fr.rewrite.server.cucumber;

import fr.rewrite.server.RewriteServerApp;
import fr.rewrite.server.cucumber.CucumberConfiguration.CucumberRestTemplateConfiguration;
import fr.rewrite.server.cucumber.rest.CucumberRestTemplate;
import fr.rewrite.server.cucumber.rest.CucumberRestTestContext;
import fr.rewrite.server.domain.RewriteConfig;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.spring.CucumberContextConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("test")
@CucumberContextConfiguration
@SpringBootTest(classes = { RewriteServerApp.class, CucumberRestTemplateConfiguration.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
public class CucumberConfiguration {

  private static final Logger log = LoggerFactory.getLogger(CucumberConfiguration.class);
  private final TestRestTemplate rest;
  private static RewriteConfig staticConfig;

  CucumberConfiguration(TestRestTemplate rest, RewriteConfig config) {
    this.rest = rest;
    staticConfig = config;
  }

  @Before
  public void resetTestContext() {
    CucumberRestTestContext.reset();
  }

  @Before
  public void loadInterceptors() {
    RestTemplate template = rest.getRestTemplate();
    ClientHttpRequestFactory existingFactory = template.getRequestFactory();

    ClientHttpRequestFactory bufferingFactory = new BufferingClientHttpRequestFactory(existingFactory);

    template.setRequestFactory(bufferingFactory);
    template.setInterceptors(List.of(saveLastResultInterceptor()));
    template.getMessageConverters().addFirst(new StringHttpMessageConverter(StandardCharsets.UTF_8));
  }

  @AfterAll
  public static void clean() throws IOException {
    try {
      deleteRecursively(staticConfig.workDirectory().getParent());
      deleteRecursively(staticConfig.configDirectory().getParent());
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  private static void deleteRecursively(Path path) throws IOException {
    Files.walkFileTree(
      path,
      new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }
      }
    );
  }

  private ClientHttpRequestInterceptor saveLastResultInterceptor() {
    return (request, body, execution) -> {
      ClientHttpResponse response = execution.execute(request, body);

      CucumberRestTestContext.addResponse(request, response, execution, body);

      return response;
    };
  }

  @TestConfiguration
  static class CucumberRestTemplateConfiguration {

    @Value("${server.ssl.enabled}")
    private boolean ssl;

    @Bean
    public TestRestTemplate testRestTemplate(RestTemplateBuilder restTemplateBuilder, SslBundles sslBundles) {
      if (ssl) {
        return new TestRestTemplate(restTemplateBuilder.sslBundle(sslBundles.getBundle("client1_cert")));
      } else {
        return new TestRestTemplate(restTemplateBuilder);
      }
    }

    @Bean
    CucumberRestTemplate cucumberRestTemplate(TestRestTemplate rest, ApplicationContext applicationContext) {
      return new CucumberRestTemplate(rest, applicationContext);
    }
  }
}
