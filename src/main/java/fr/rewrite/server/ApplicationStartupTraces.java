package fr.rewrite.server;

import fr.rewrite.server.shared.error.domain.Assert;
import fr.rewrite.server.shared.generation.domain.ExcludeFromGeneratedCodeCoverage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

final class ApplicationStartupTraces {

  private static final String SEPARATOR = "-".repeat(58);
  private static final String BREAK = "\n";

  private static final Logger log = LoggerFactory.getLogger(ApplicationStartupTraces.class);

  private ApplicationStartupTraces() {}

  static String of(Environment environment) {
    Assert.notNull("environment", environment);

    return new ApplicationStartupTracesBuilder()
      .append(BREAK)
      .appendSeparator()
      .append(applicationRunningTrace(environment))
      .append(localUrl(environment))
      .append(externalUrl(environment))
      .append(profilesTrace(environment))
      .appendSeparator()
      .append(configServer(environment))
      .build();
  }

  private static String applicationRunningTrace(Environment environment) {
    String applicationName = environment.getProperty("spring.application.name");

    if (StringUtils.isBlank(applicationName)) {
      return "Application is running!";
    }

    return new StringBuilder().append("Application '").append(applicationName).append("' is running!").toString();
  }

  private static String localUrl(Environment environment) {
    return url("Local", "localhost", environment);
  }

  private static String externalUrl(Environment environment) {
    return url("External", hostAddress(), environment);
  }

  private static String url(String type, String host, Environment environment) {
    if (notWebEnvironment(environment)) {
      return null;
    }

    return new StringBuilder()
      .append(type)
      .append(": \t")
      .append(protocol(environment))
      .append("://")
      .append(host)
      .append(":")
      .append(port(environment))
      .append(contextPath(environment))
      .toString();
  }

  private static boolean notWebEnvironment(Environment environment) {
    return StringUtils.isBlank(environment.getProperty("server.port"));
  }

  private static String protocol(Environment environment) {
    if (noKeyStore(environment)) {
      return "http";
    }

    return "https";
  }

  private static boolean noKeyStore(Environment environment) {
    return StringUtils.isBlank(environment.getProperty("server.ssl.key-store"));
  }

  private static String port(Environment environment) {
    return environment.getProperty("server.port");
  }

  private static String profilesTrace(Environment environment) {
    String[] profiles = environment.getActiveProfiles();

    if (ArrayUtils.isEmpty(profiles)) {
      return null;
    }

    return "Profile(s): \t%s".formatted(String.join(", ", profiles));
  }

  @ExcludeFromGeneratedCodeCoverage(reason = "Hard to test implement detail error management")
  private static String hostAddress() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      log.warn("The host name could not be determined, using `localhost` as fallback");
    }

    return "localhost";
  }

  private static String contextPath(Environment environment) {
    String contextPath = environment.getProperty("server.servlet.context-path");

    if (StringUtils.isBlank(contextPath)) {
      return "/";
    }

    return contextPath;
  }

  private static String configServer(Environment environment) {
    String configServer = environment.getProperty("configserver.status");

    if (StringUtils.isBlank(configServer)) {
      return null;
    }

    return new StringBuilder().append("Config Server: ").append(configServer).append(BREAK).append(SEPARATOR).append(BREAK).toString();
  }

  private static final class ApplicationStartupTracesBuilder {

    private static final String SPACER = "  ";

    private final StringBuilder trace = new StringBuilder();

    public ApplicationStartupTracesBuilder appendSeparator() {
      trace.append(SEPARATOR).append(BREAK);

      return this;
    }

    public ApplicationStartupTracesBuilder append(String line) {
      if (line == null) {
        return this;
      }

      trace.append(SPACER).append(line).append(BREAK);

      return this;
    }

    public String build() {
      return trace.toString();
    }
  }
}
