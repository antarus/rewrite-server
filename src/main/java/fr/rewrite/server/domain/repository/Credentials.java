package fr.rewrite.server.domain.repository;

public record Credentials(
  String username,
  // Personal Access Token
  String pat
) {}
