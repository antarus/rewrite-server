package fr.rewrite.server.domain.datastore;

import fr.rewrite.server.shared.error.domain.ErrorKey;

public enum DatastoreErrorKey implements ErrorKey {
  DATASTORE_OPERATION_ERROR("datastore-operation-error"),
  DATASTORE_ALREADY_EXIST("datastore-already-exist"),
  DATASTORE_NOT_FOUND("datastore-not-found"),
  DATASTORE_NOT_VALID("datastore-not-valid"),
  INVALID_STATE_TRANSITION("invalid-state-transition");

  private final String key;

  DatastoreErrorKey(String key) {
    this.key = key;
  }

  @Override
  public String get() {
    return key;
  }
}
