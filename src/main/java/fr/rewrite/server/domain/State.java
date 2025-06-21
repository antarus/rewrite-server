package fr.rewrite.server.domain;

import java.time.LocalDateTime;

public record State(StateEnum status, LocalDateTime createdAt, LocalDateTime updatedAt) {
  public static State init() {
    return new State(StateEnum.INIT, LocalDateTime.now(), LocalDateTime.now());
  }
}
