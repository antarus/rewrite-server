package fr.rewrite.server.domain;

import java.util.Set;

public enum StatusEnum {
  INIT,
  DATASTORE_CREATED,
  DATASTORE_FAILED,
  REPOSITORY_DELETING,
  REPOSITORY_DELETED,
  REPOSITORY_DELETE_FAILED,
  REPOSITORY_CLONING,
  REPOSITORY_CLONED,
  REPOSITORY_CLONE_FAILED,
  BRANCH_CREATING,
  BRANCH_CREATED,
  BRANCH_CREATION_FAILED,
  BUILDING,
  BUILD,
  BUILD_FAILED,
  CLASSPATH_GETTING,
  CLASSPATH,
  CLASSPATH_FAILED;

  public boolean canTransitionTo(StatusEnum nextState) {
    return switch (this) {
      case INIT -> Set.of(DATASTORE_CREATED).contains(nextState);
      case DATASTORE_CREATED -> Set.of(REPOSITORY_CLONING, DATASTORE_FAILED, REPOSITORY_DELETING).contains(nextState);
      case REPOSITORY_CLONING -> Set.of(REPOSITORY_CLONED, REPOSITORY_CLONE_FAILED).contains(nextState);
      case REPOSITORY_CLONED -> Set.of(BRANCH_CREATING, BUILDING, CLASSPATH_GETTING).contains(nextState);
      case REPOSITORY_CLONE_FAILED -> Set.of(REPOSITORY_CLONING, REPOSITORY_DELETING).contains(nextState);
      case REPOSITORY_DELETING -> Set.of(REPOSITORY_DELETED).contains(nextState);
      case REPOSITORY_DELETED -> Set.of(REPOSITORY_CLONING).contains(nextState);
      case BRANCH_CREATING -> Set.of(BRANCH_CREATED, BRANCH_CREATION_FAILED).contains(nextState);
      case BRANCH_CREATED -> Set.of(BUILDING, REPOSITORY_DELETING).contains(nextState);
      case BUILDING -> Set.of(BUILD, BUILD_FAILED).contains(nextState);
      case BUILD -> Set.of(BUILDING, CLASSPATH_GETTING).contains(nextState);
      case CLASSPATH_GETTING -> Set.of(CLASSPATH, CLASSPATH_FAILED).contains(nextState);
      case CLASSPATH -> Set.of(CLASSPATH_GETTING, BUILDING).contains(nextState);
      case REPOSITORY_DELETE_FAILED, BRANCH_CREATION_FAILED, BUILD_FAILED, CLASSPATH_FAILED, DATASTORE_FAILED -> false;
    };
  }
}
