package fr.rewrite.server.cucumber;

import fr.rewrite.server.cucumber.rest.CucumberRestTemplate;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

public class UpdaterSteps {

  @Autowired
  private CucumberRestTemplate rest;

  @When("I update repository {string} with recipe {string}")
  public void iUpdateRepositoryFor(String uuid, String recipe) {
    rest.post(
      "/api/updater",
      """
       {
           "datastoreId" : {
               "uuid": "%"
           },
           "recipe" : {
               "name": "%s"
           }
       }
      """.formatted(uuid, recipe)
    );
  }
}
