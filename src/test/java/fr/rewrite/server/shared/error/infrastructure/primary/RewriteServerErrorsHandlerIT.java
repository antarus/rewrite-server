package fr.rewrite.server.shared.error.infrastructure.primary;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import fr.rewrite.server.IntegrationTest;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class RewriteServerErrorsHandlerIT {

  @Autowired
  private MockMvc rest;

  @Test
  void shouldHandleRewriteServerErrorWithoutLocale() throws Exception {
    rest
      .perform(get("/api/errors/bad-request"))
      .andExpect(status().is4xxClientError())
      .andExpect(jsonPath("title").value("Bad request"))
      .andExpect(jsonPath("detail").value("You send a bad request: 400"));
  }
}
