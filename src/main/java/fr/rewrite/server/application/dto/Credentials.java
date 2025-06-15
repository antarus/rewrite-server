package fr.rewrite.server.application.dto;

public record Credentials(
  String username,
  String pat // Personal Access Token
) {}
