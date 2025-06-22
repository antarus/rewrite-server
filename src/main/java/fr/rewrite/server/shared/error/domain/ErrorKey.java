package fr.rewrite.server.shared.error.domain;

import java.io.Serializable;

public interface ErrorKey extends Serializable {
  String get();
}
