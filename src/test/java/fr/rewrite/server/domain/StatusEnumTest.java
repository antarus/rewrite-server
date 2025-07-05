package fr.rewrite.server.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.rewrite.server.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class StatusEnumTest {

  @Test
  void shouldAllowTransitionFromInitToDatastoreCreated() {
    assertThat(StatusEnum.INIT.canTransitionTo(StatusEnum.DATASTORE_CREATED)).isTrue();
  }

  @Test
  void shouldNotAllowTransitionFromInitToRepositoryCloning() {
    assertThat(StatusEnum.INIT.canTransitionTo(StatusEnum.REPOSITORY_CLONING)).isFalse();
  }

  @Test
  void shouldAllowTransitionFromDatastoreCreatedToRepositoryCloning() {
    assertThat(StatusEnum.DATASTORE_CREATED.canTransitionTo(StatusEnum.REPOSITORY_CLONING)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromDatastoreCreatedToDatastoreFailed() {
    assertThat(StatusEnum.DATASTORE_CREATED.canTransitionTo(StatusEnum.DATASTORE_FAILED)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromDatastoreCreatedToRepositoryDeleting() {
    assertThat(StatusEnum.DATASTORE_CREATED.canTransitionTo(StatusEnum.REPOSITORY_DELETING)).isTrue();
  }

  @Test
  void shouldNotAllowTransitionFromDatastoreCreatedToInit() {
    assertThat(StatusEnum.DATASTORE_CREATED.canTransitionTo(StatusEnum.INIT)).isFalse();
  }

  @Test
  void shouldAllowTransitionFromRepositoryCloningToRepositoryCloned() {
    assertThat(StatusEnum.REPOSITORY_CLONING.canTransitionTo(StatusEnum.REPOSITORY_CLONED)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromRepositoryCloningToRepositoryCloneFailed() {
    assertThat(StatusEnum.REPOSITORY_CLONING.canTransitionTo(StatusEnum.REPOSITORY_CLONE_FAILED)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromRepositoryClonedToBranchCreating() {
    assertThat(StatusEnum.REPOSITORY_CLONED.canTransitionTo(StatusEnum.BRANCH_CREATING)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromRepositoryClonedToBuilding() {
    assertThat(StatusEnum.REPOSITORY_CLONED.canTransitionTo(StatusEnum.BUILDING)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromRepositoryClonedToClasspathGetting() {
    assertThat(StatusEnum.REPOSITORY_CLONED.canTransitionTo(StatusEnum.CLASSPATH_GETTING)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromRepositoryCloneFailedToRepositoryCloning() {
    assertThat(StatusEnum.REPOSITORY_CLONE_FAILED.canTransitionTo(StatusEnum.REPOSITORY_CLONING)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromRepositoryCloneFailedToRepositoryDeleting() {
    assertThat(StatusEnum.REPOSITORY_CLONE_FAILED.canTransitionTo(StatusEnum.REPOSITORY_DELETING)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromRepositoryDeletingToRepositoryDeleted() {
    assertThat(StatusEnum.REPOSITORY_DELETING.canTransitionTo(StatusEnum.REPOSITORY_DELETED)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromRepositoryDeletedToRepositoryCloning() {
    assertThat(StatusEnum.REPOSITORY_DELETED.canTransitionTo(StatusEnum.REPOSITORY_CLONING)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromBranchCreatingToBranchCreated() {
    assertThat(StatusEnum.BRANCH_CREATING.canTransitionTo(StatusEnum.BRANCH_CREATED)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromBranchCreatingToBranchCreationFailed() {
    assertThat(StatusEnum.BRANCH_CREATING.canTransitionTo(StatusEnum.BRANCH_CREATION_FAILED)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromBranchCreatedToBuilding() {
    assertThat(StatusEnum.BRANCH_CREATED.canTransitionTo(StatusEnum.BUILDING)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromBranchCreatedToRepositoryDeleting() {
    assertThat(StatusEnum.BRANCH_CREATED.canTransitionTo(StatusEnum.REPOSITORY_DELETING)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromBuildingToBuild() {
    assertThat(StatusEnum.BUILDING.canTransitionTo(StatusEnum.BUILD)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromBuildingToBuildFailed() {
    assertThat(StatusEnum.BUILDING.canTransitionTo(StatusEnum.BUILD_FAILED)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromBuildToBuilding() {
    assertThat(StatusEnum.BUILD.canTransitionTo(StatusEnum.BUILDING)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromBuildToClasspathGetting() {
    assertThat(StatusEnum.BUILD.canTransitionTo(StatusEnum.CLASSPATH_GETTING)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromClasspathGettingToClasspath() {
    assertThat(StatusEnum.CLASSPATH_GETTING.canTransitionTo(StatusEnum.CLASSPATH)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromClasspathGettingToClasspathFailed() {
    assertThat(StatusEnum.CLASSPATH_GETTING.canTransitionTo(StatusEnum.CLASSPATH_FAILED)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromClasspathToClasspathGetting() {
    assertThat(StatusEnum.CLASSPATH.canTransitionTo(StatusEnum.CLASSPATH_GETTING)).isTrue();
  }

  @Test
  void shouldAllowTransitionFromClasspathToBuilding() {
    assertThat(StatusEnum.CLASSPATH.canTransitionTo(StatusEnum.BUILDING)).isTrue();
  }

  @Test
  void shouldNotAllowTransitionFromFailedStates() {
    assertThat(StatusEnum.REPOSITORY_DELETE_FAILED.canTransitionTo(StatusEnum.INIT)).isFalse();
    assertThat(StatusEnum.BRANCH_CREATION_FAILED.canTransitionTo(StatusEnum.INIT)).isFalse();
    assertThat(StatusEnum.BUILD_FAILED.canTransitionTo(StatusEnum.INIT)).isFalse();
    assertThat(StatusEnum.CLASSPATH_FAILED.canTransitionTo(StatusEnum.INIT)).isFalse();
    assertThat(StatusEnum.DATASTORE_FAILED.canTransitionTo(StatusEnum.INIT)).isFalse();
  }
}
