package fr.rewrite.server.domain.log;

import fr.rewrite.server.domain.RewriteConfig;
import fr.rewrite.server.shared.error.domain.Assert;
import java.util.regex.Pattern;

public record CleanSensitiveLog(RewriteConfig config) {
  // Regex to specifically target and replace the /home/<user> part.
  // This will match "/home/user" regardless of what follows it.
  private static final Pattern HOME_USER_PATH_PATTERN = Pattern.compile(
    "(/home/[^/]+)" // Matches "/home/user"
  );

  public CleanSensitiveLog {
    Assert.notNull("config", config);
  }

  public String clean(String message) {
    if (message == null) {
      return null;
    }
    message = maskMavenHomePath(message);
    message = maskBaseDirectory(message);

    return message;
  }

  public String maskBaseDirectory(String msgToSantize) {
    Assert.notNull("msgToSantize", msgToSantize);

    String normalizedWorkDirectory = config.workDirectory().normalize().toString();
    String normalizedConfigDirectory = config.configDirectory().normalize().toString();

    if (msgToSantize.contains(normalizedWorkDirectory)) {
      msgToSantize = msgToSantize.replace(normalizedWorkDirectory, "[ *** ]");
    }
    if (msgToSantize.contains(normalizedConfigDirectory)) {
      msgToSantize = msgToSantize.replace(normalizedConfigDirectory, "[ *** ]");
    }

    return msgToSantize;
  }
  public String maskMavenHomePath(String msgToSanitize) {
    Assert.notNull("msgToSanitize", msgToSanitize);

    return msgToSanitize.replaceAll(HOME_USER_PATH_PATTERN.pattern(), "[ *** ]");
  }
}
