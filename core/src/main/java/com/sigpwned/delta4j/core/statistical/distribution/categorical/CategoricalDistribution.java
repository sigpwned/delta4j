/*-
 * =================================LICENSE_START==================================
 * delta4j-core
 * ====================================SECTION=====================================
 * Copyright (C) 2024 Andy Boothe
 * ====================================SECTION=====================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==================================LICENSE_END===================================
 */
package com.sigpwned.delta4j.core.statistical.distribution.categorical;

import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * <p>
 * A categorical distribution is a probability distribution over a finite set of categories. Each
 * category has a probability of being chosen, and the probabilities sum to 1. The distribution can
 * be used to sample random elements from the set of categories.
 * </p>
 *
 * <p>
 * The distribution is represented as a map from CDF (Cumulative Density Function) to categories. As
 * a result, sampling from the distribution is O(log n) where n is the number of categories.
 * Category values do not have to be mutually comparable, but they must be non-null, support the
 * equals method, and have a consistent hash code.
 * </p>
 *
 * <p>
 * The distribution can be created from a set of elements, a stream of elements, or a stream of
 * pairs of elements and counts. All of these methods are O(n log n), where n is the number of
 * elements in the method's respective stream or container.
 * </p>
 *
 * <p>
 * Once built, instances of this class are immutable, and therefore thread-safe. When sampling from
 * the distribution in a concurrent manner, bear in mind that the random number generator must be
 * thread-safe, too. Therefore, it is generally recommended to use {@link ThreadLocalRandom} when
 * sampling.
 * </p>
 *
 * @param <T> the type of the categories
 */
public class CategoricalDistribution<T> {

  /**
   * A sketch of a stream of data for fitting a categorical distribution. A sketch is a mutable
   * container that accumulates the data necessary to fit a categorical distribution.
   *
   * @param <T> the type of the categories
   */
  public static class Sketch<T> implements Consumer<T> {

    private final Map<T, Long> distribution;

    public Sketch() {
      this.distribution = new HashMap<>();
    }

    /**
     * Create a sketch from a distribution. The distribution is used as the initial state of the new
     * sketch. Note that elements with zero count are ignored.
     *
     * @param distribution a distribution
     * @throws NullPointerException     if distribution is null or contains a null key or value
     * @throws IllegalArgumentException if distribution contains a negative count
     */
    public Sketch(Map<T, Long> distribution) {
      if (distribution == null) {
        throw new NullPointerException();
      }
      this.distribution = new HashMap<>();
      for (Map.Entry<T, Long> e : distribution.entrySet()) {
        if (e.getKey() == null || e.getValue() == null) {
          throw new NullPointerException();
        } else if (e.getValue() < 0L) {
          throw new IllegalArgumentException("distribution counts must be positive");
        } else if (e.getValue() == 0) {
          // ignore zero counts
        } else {
          this.distribution.put(e.getKey(), e.getValue());
        }
      }
    }

    public Map<T, Long> categories() {
      return unmodifiableMap(distribution);
    }

    public void merge(Sketch<T> that) {
      for (Map.Entry<T, Long> e : that.distribution.entrySet()) {
        distribution.merge(e.getKey(), e.getValue(), Long::sum);
      }
    }

    /**
     * Accept a single element with a count of 1.
     *
     * @param t the element
     * @throws NullPointerException if t is null
     */
    @Override
    public void accept(T t) {
      if (t == null) {
        throw new NullPointerException();
      }
      accept(t, 1L);
    }

    /**
     * Accept a single element with a count. If the count is zero, then the element is ignored.
     *
     * @param t     the element
     * @param count the count
     * @throws NullPointerException     if t is null
     * @throws IllegalArgumentException if count is negative
     */
    public void accept(T t, long count) {
      if (t == null) {
        throw new NullPointerException();
      }
      if (count < 0L) {
        throw new IllegalArgumentException("distribution counts must be positive");
      }
      if (count > 0L) {
        distribution.merge(t, count, Long::sum);
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Sketch<?> sketch)) {
        return false;
      }
      return Objects.equals(distribution, sketch.distribution);
    }

    @Override
    public int hashCode() {
      return Objects.hash(distribution);
    }

    @Override
    public String toString() {
      return "Sketch{" + "distribution=" + distribution + '}';
    }
  }

  /**
   * Collect a stream of elements into a categorical distribution. The resulting distribution will
   * have one category for each unique element in the stream, and the probability of each category
   * will be proportional to the number of occurrences of that element in the stream. Null elements
   * are not allowed.
   *
   * @param <T> the type of the elements
   * @return a new collector
   * @throws NullPointerException if the stream contains a null element
   * @see #fitUniform(Set)
   */
  public static <T> Collector<T, Sketch<T>, CategoricalDistribution<T>> toUniformCategoricalDistribution() {
    return Collector.of(Sketch::new, Sketch::accept, (a, b) -> {
      a.merge(b);
      return a;
    }, CategoricalDistribution::fromSketch);
  }

  /**
   * Collect a stream of elements into a categorical distribution. The resulting distribution will
   * have one category for each unique element in the stream, and the probability of each category
   * will be proportional to the number of occurrences of that element in the stream. Null elements
   * are not allowed.
   *
   * @param <T> the type of the elements
   * @return a new collector
   * @throws NullPointerException if the stream contains a null element
   * @see #fitEmpiricalFromOccurrences(Stream)
   */
  public static <T> Collector<T, Sketch<T>, CategoricalDistribution<T>> toEmpiricalCategoricalDistributionFromOccurrences() {
    return Collector.of(Sketch::new, Sketch::accept, (a, b) -> {
      a.merge(b);
      return a;
    }, CategoricalDistribution::fromSketch);
  }

  /**
   * Collect a stream of (element, count) pairs into a categorical distribution. The resulting
   * distribution will have one category for each unique key in the stream, and the probability of
   * each category will be proportional to the sum of the counts corresponding to that key in the
   * stream. Null elements, keys, or values are not allowed. Categories with total count 0 are
   * ignored. The counts must be non-negative.
   *
   * @param <T> the type of the elements
   * @return a new collector
   * @throws NullPointerException     if the stream contains a null element, key, or value
   * @throws IllegalArgumentException if the stream contains a negative count
   * @see #fitEmpiricalFromCounts(Stream)
   */
  public static <T> Collector<Map.Entry<T, Long>, Sketch<T>, CategoricalDistribution<T>> toEmpiricalCategoricalDistributionFromCounts() {
    return Collector.of(Sketch::new, (m, t) -> {
      m.accept(t.getKey(), t.getValue());
    }, (a, b) -> {
      a.merge(b);
      return a;
    }, CategoricalDistribution::fromSketch);
  }

  public static <T> CategoricalDistribution<T> fromSketch(Sketch<T> sketch) {
    return of(sketch.distribution);
  }

  /**
   * Create a uniform categorical distribution from a set of elements. The resulting distribution
   * will have one category for each element in the set, and equal probability for each category.
   *
   * @param xs  a set of elements
   * @param <T> the type of the elements
   * @return a new distribution
   * @throws NullPointerException if xs is null or contains a null element
   */
  public static <T> CategoricalDistribution<T> fitUniform(Set<T> xs) {
    return fitEmpiricalFromOccurrences(requireNonNull(xs).stream());
  }

  /**
   * Create a uniform categorical distribution from a stream of elements. The resulting distribution
   * will have one category for each element in the set, and equal probability for each category.
   *
   * @param stream a stream of elements
   * @param <T>    the type of the elements
   * @return a new distribution
   * @throws NullPointerException if xs is null or contains a null element
   */
  public static <T> CategoricalDistribution<T> fitUniform(Stream<T> stream) {
    return fitEmpiricalFromOccurrences(stream.distinct());
  }

  /**
   * Fit an empirical categorical distribution to a stream of elements. The relative probabilities
   * of each element are determined by the number of occurrences of each element in the stream. The
   * resulting distribution will have one category for each unique element in the stream.
   *
   * @param stream a stream of elements
   * @param <T>    the type of the elements
   * @return a new distribution
   * @throws NullPointerException if stream is null or contains a null element
   */
  public static <T> CategoricalDistribution<T> fitEmpiricalFromOccurrences(Stream<T> stream) {
    return requireNonNull(stream).collect(toEmpiricalCategoricalDistributionFromOccurrences());
  }

  /**
   * Fit an empirical categorical distribution to a stream of (element, count) pairs. The relative
   * probabilities of each element are determined by the sum of the counts corresponding to each
   * unique key in the stream. The resulting distribution will have one category for each unique key
   * in the stream. Categories with total count 0 are ignored.
   *
   * @param stream a stream of elements
   * @param <T>    the type of the elements
   * @return a new distribution
   * @throws NullPointerException     if stream is null or contains a null element, key, or value
   * @throws IllegalArgumentException if stream contains a negative count
   */
  public static <T> CategoricalDistribution<T> fitEmpiricalFromCounts(
      Stream<Map.Entry<T, Long>> stream) {
    return requireNonNull(stream).collect(toEmpiricalCategoricalDistributionFromCounts());
  }

  /**
   * Create an empirical categorical distribution from a map of elements to counts. These counts
   * determine the relative probabilities of each element. Categories with count 0 are ignored.
   *
   * @param distribution a map of elements to counts
   * @param <T>          the type of the elements
   * @return a new distribution
   * @throws NullPointerException     if distribution is null or contains a null key or value
   * @throws IllegalArgumentException if distribution contains a negative count
   * @see #CategoricalDistribution(Map)
   */
  public static <T> CategoricalDistribution<T> of(Map<T, Long> distribution) {
    return new CategoricalDistribution<>(distribution);
  }

  private final NavigableMap<Long, T> distribution;
  private final long total;

  /**
   * Create a categorical distribution from a map of elements to counts. These counts determine the
   * relative probabilities of each element. Categories with count 0 are ignored.
   *
   * @param distribution a map of elements to counts
   * @throws NullPointerException     if distribution is null or contains a null key
   * @throws IllegalArgumentException if distribution is empty or contains a negative count
   */
  public CategoricalDistribution(Map<T, Long> distribution) {
    if (distribution == null) {
      throw new NullPointerException();
    }
    if (distribution.isEmpty()) {
      throw new IllegalArgumentException("distribution must not be empty");
    }

    this.distribution = new TreeMap<>();

    // The distribution is stored as mapping from CDF to category, where the CDF for each category
    // is the cumulative density of weights for categories up to but not including the current category.
    // This allows us to sample in O(log n) time using NavigableMap#floorEntry.
    long total = 0L;
    for (Map.Entry<T, Long> e : distribution.entrySet()) {
      if (e.getKey() == null || e.getValue() == null) {
        throw new NullPointerException();
      } else if (e.getValue() < 0L) {
        throw new IllegalArgumentException("distribution counts must be positive");
      } else if (e.getValue() == 0L) {
        // ignore zero counts
      } else {
        this.distribution.put(total, e.getKey());
        total += e.getValue();
      }
    }

    this.total = total;
  }

  private CategoricalDistribution(NavigableMap<Long, T> distribution, long total) {
    this.distribution = distribution;
    this.total = total;
  }

  /**
   * Choose a weighted random element from the distribution.
   *
   * @param r random number generator
   * @return a random element
   * @throws NullPointerException if r is null
   */
  public T sample(Random r) {
    if (r == null) {
      throw new NullPointerException();
    }

    long x = r.nextLong(total);
    Map.Entry<Long, T> entry = distribution.floorEntry(x);
    if (entry == null) {
      throw new AssertionError("entry is null");
    }

    return entry.getValue();
  }

  /**
   * Maps a distribution of T to a distribution of U. Care should be taken to ensure that the
   * mapping function is injective (i.e., one-to-one). Otherwise, the resulting distribution will
   * not have the same relative probabilities. This is not enforced by the implementation.
   *
   * @param f   the mapping function from T to U
   * @param <U> the type of the resulting distribution
   * @return a new distribution of U
   * @throws NullPointerException if f is null or f(t) is null for some t
   */
  public <U> CategoricalDistribution<U> map(Function<T, U> f) {
    if (f == null) {
      throw new NullPointerException();
    }
    return new CategoricalDistribution<>(distribution.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> requireNonNull(f.apply(e.getValue())), (a, b) -> {
          // This should never happen since we're not changing keys
          throw new AssertionError("duplicate key");
        }, TreeMap::new)), total);
  }

  /**
   * Returns the number of categories in the distribution.
   *
   * @return the number of elements
   */
  public int size() {
    return distribution.size();
  }

  /**
   * Returns the underlying frequency data of this distribution as an unmodifiable map. The map is a
   * copy of the internal data structure, and changes to the map will not affect the distribution.
   *
   * @return the total number of occurrences
   */
  public Stream<Map.Entry<T, Long>> categories() {
    // This is O(n log n) but it's the best we can do in the current Java version. Access to
    // Gatherers from JEP 461 (https://openjdk.org/jeps/461) would make this more efficient with
    // O(1) sliding window logic, making this a O(n) operation.
    return distribution.entrySet().stream().map(e -> {
      Map.Entry<Long, T> lower = distribution.lowerEntry(e.getKey());
      long count = lower == null ? e.getKey() : e.getKey() - lower.getKey();
      return Map.entry(e.getValue(), count);
    });
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CategoricalDistribution<?> that)) {
      return false;
    }
    return total == that.total && Objects.equals(distribution, that.distribution);
  }

  @Override
  public int hashCode() {
    return Objects.hash(distribution, total);
  }

  @Override
  public String toString() {
    return "CategoricalDistribution{" + "distribution=" + distribution + ", total=" + total + '}';
  }
}
