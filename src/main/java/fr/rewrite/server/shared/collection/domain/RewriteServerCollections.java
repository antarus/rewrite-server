package fr.rewrite.server.shared.collection.domain;

import java.util.*;

/**
 * Null safe utility class to manage collections
 */
public final class RewriteServerCollections {

  private RewriteServerCollections() {}

  /**
   * Get an immutable collection from the given collection
   *
   * @param <T>
   *          Type of this collection
   * @param collection
   *          input collection
   * @return An immutable collection
   */
  public static <T> Collection<T> immutable(Collection<? extends T> collection) {
    if (collection == null) {
      return Set.of();
    }

    return Collections.unmodifiableCollection(collection);
  }

  /**
   * Get an immutable set from the given set
   *
   * @param <T>
   *          Type of this set
   * @param set
   *          input set
   * @return An immutable set
   */
  public static <T> Set<T> immutable(Set<? extends T> set) {
    if (set == null) {
      return Set.of();
    }

    return Collections.unmodifiableSet(set);
  }

  /**
   * Get an immutable set from the given list
   *
   * @param <T>
   *          Type of this list
   * @param list
   *          input list
   * @return An immutable list
   */
  public static <T> List<T> immutable(List<? extends T> list) {
    if (list == null) {
      return List.of();
    }

    return Collections.unmodifiableList(list);
  }

  /**
   * Get an immutable map from the given map
   *
   * @param <K> Key type of this map
   * @param <V> value type of this map
   * @return An immutable map
   */
  public static <K, V> Map<K, V> immutable(Map<? extends K, ? extends V> map) {
    if (map == null) {
      return Map.of();
    }
    return Collections.unmodifiableMap(map);
  }
}
