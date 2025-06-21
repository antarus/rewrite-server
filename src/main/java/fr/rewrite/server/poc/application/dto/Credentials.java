package fr.rewrite.server.poc.application.dto;

public record Credentials(
  String username,
  String pat // Personal Access Token
) {}
