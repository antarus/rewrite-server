package fr.rewrite.server.domain.repository;

public record Credentials(
  String username,
  // Personal Access Token
  String pat
) {
  @Override
  public String toString() {
    return "Credentials{" + "username='" + username + '\'' + ", pat='[ *** Mask ***]'" + '}';
  }
}
