package fr.rewrite.server.shared.error_generator.infrastructure.primary;

import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/bean-validation-errors")
class BeanValidationErrorsResource {

  @PostMapping("mandatory-body-parameter")
  void createMandatoryParameter(@RequestBody @Validated RestMandatoryParameter parameter) {
    // empty method
  }

  @GetMapping("complicated-path-variable/{complicated}")
  void complicatedPathVariable(@PathVariable("complicated") @Pattern(regexp = "complicated") String complicated) {
    // empty method
  }
}
