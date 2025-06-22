package fr.rewrite.server.shared.error_generator.infrastructure.primary;

import fr.rewrite.server.shared.error.domain.RewriteServerException;
import fr.rewrite.server.shared.error.domain.StandardErrorKey;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/errors")
class RewriteServerErrorsResource {

  @GetMapping("bad-request")
  void getBadRequest() {
    throw RewriteServerException.badRequest(StandardErrorKey.BAD_REQUEST).addParameter("code", "400").build();
  }
}
