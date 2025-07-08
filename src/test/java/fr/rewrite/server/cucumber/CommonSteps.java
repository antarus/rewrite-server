package fr.rewrite.server.cucumber;

import static fr.rewrite.server.cucumber.rest.CucumberRestAssertions.assertThatLastResponse;

import fr.rewrite.server.cucumber.rest.CucumberRestTemplate;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

public class CommonSteps {

  @Autowired
  private CucumberRestTemplate rest;

  @Then("I should have a bean")
  public void shouldGetResponseContent(Map<String, Object> response) {
    assertThatLastResponse().hasResponse().containing(response);
  }

  @Then("Http code must be {int}")
  public void responseCode(int status) {
    //    assertThatLastResponse().debug();
    assertThatLastResponse().hasHttpStatus(status);
  }
}
