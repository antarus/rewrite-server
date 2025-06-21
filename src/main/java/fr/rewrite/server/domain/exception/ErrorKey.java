package fr.rewrite.server.domain.exception;

import java.io.Serializable;

public interface ErrorKey extends Serializable {
  String get();
}
