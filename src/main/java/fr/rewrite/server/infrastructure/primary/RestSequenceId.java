package fr.rewrite.server.infrastructure.primary;

import fr.rewrite.server.domain.SequenceId;
import fr.rewrite.server.domain.datastore.DatastoreId;
import fr.rewrite.server.shared.error.domain.Assert;
import org.jetbrains.annotations.NotNull;

public record RestSequenceId(int value) implements Comparable<fr.rewrite.server.domain.SequenceId> {
  public RestSequenceId {
    Assert.field("value", value).positive();
  }
  @Override
  public int compareTo(@NotNull SequenceId o) {
    return 0;
  }
  public static RestSequenceId from(SequenceId sequenceId) {
    return new RestSequenceId(sequenceId.value());
  }
}
