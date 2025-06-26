package fr.rewrite.server.domain.state;

import java.util.Set;

public enum StateEnum {
  INIT,
  DATASTORE_CREATED,
  CLONING,
  CLONED,
  CLONE_FAILED,
  BRANCH_CREATING,
  BRANCH_CREATED,
  BRANCH_CREATION_FAILED,
  BUILDING,
  BUILD,
  BUILD_FAILED;

  public boolean canTransitionTo(StateEnum nextState) {
    return switch (this) {
      case INIT -> Set.of(CLONING).contains(nextState);
      case DATASTORE_CREATED -> Set.of(CLONING).contains(nextState);
      case CLONING -> Set.of(CLONED, CLONE_FAILED).contains(nextState);
      case CLONED -> Set.of(BRANCH_CREATING, BUILDING).contains(nextState);
      case BRANCH_CREATING -> Set.of(BRANCH_CREATED, BRANCH_CREATION_FAILED).contains(nextState);
      case BRANCH_CREATED -> Set.of(BUILDING).contains(nextState);
      case BUILDING -> Set.of(BUILD, BUILD_FAILED).contains(nextState);
      case BUILD -> Set.of(BUILDING).contains(nextState);
      case CLONE_FAILED, BRANCH_CREATION_FAILED, BUILD_FAILED -> false;
    };
  }
}
