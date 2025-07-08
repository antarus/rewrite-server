package fr.rewrite.server.cucumber;

import static fr.rewrite.server.cucumber.rest.CucumberRestAssertions.assertThatLastResponse;

import fr.rewrite.server.cucumber.rest.CucumberRestTemplate;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

public class DatastoreSteps {

  @Autowired
  private CucumberRestTemplate rest;

  @When("I am requesting information from an datastore {string}")
  public void iAmRequestingInformationFromAnExistingDatastore(String uuid) {
    rest.get("/api/datastore/" + uuid);
  }

  @When("I create a datastore for {string}")
  public void iCreateADatastoreFor(String url) {
    rest.post(
      "/api/datastore",
      """
      { "url" : "%s" }
      """.formatted(url)
    );
  }

  @Then("I get simple response with id {string} and url {string}")
  public void shouldGetResponse(String name, String age) {
    assertThatLastResponse()
      .hasOkStatus()
      .hasElement("$.datastoreId.uuid")
      .withValue(name)
      .and()
      .hasElement("$.repositoryURL.url")
      .withValue(age);
  }
}
