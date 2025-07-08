package fr.rewrite.server.cucumber.rest;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class CucumberRestTemplate {

  private final TestRestTemplate rest;
  private final ApplicationContext applicationContext;

  //private final int port;
  public CucumberRestTemplate(TestRestTemplate rest, ApplicationContext applicationContext) {
    this.rest = rest;
    this.applicationContext = applicationContext;
  }

  public void get(String uri) {
    uri = validateUrl(uri);
    rest.getForEntity(uri, Void.class);
  }

  public void post(String uri, String content) {
    uri = validateUrl(uri);
    rest.exchange(uri, HttpMethod.POST, new HttpEntity<>(content, jsonHeaders()), Void.class);
  }

  public void put(String uri, String content) {
    uri = validateUrl(uri);
    rest.exchange(uri, HttpMethod.PUT, new HttpEntity<>(content, jsonHeaders()), Void.class);
  }

  public void delete(String uri) {
    uri = validateUrl(uri);
    rest.delete(uri);
  }

  public TestRestTemplate template() {
    return rest;
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();

    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);

    return headers;
  }

  @NotNull
  private String validateUrl(String uri) {
    int port = ((ServletWebServerApplicationContext) applicationContext).getWebServer().getPort();
    if (uri.startsWith("/")) {
      uri = "https://localhost:" + port + uri;
    }
    return uri;
  }
}
