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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
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
public class UniformDistribution<T> {

  /**
   * A sketch of a stream of data for fitting a categorical distribution. A sketch is a mutable
   * container that accumulates the data necessary to fit a categorical distribution.
   *
   * @param <T> the type of the categories
   */
  public static class Sketch<T> implements Consumer<T> {

    private final Set<T> categories;

    public Sketch() {
      this.categories = new HashSet<>();
    }

    /**
     * Create a sketch from a distribution. The distribution is used as the initial state of the new
     * sketch. Note that elements with zero count are ignored.
     *
     * @param categories the set of categories
     * @throws NullPointerException     if distribution is null or contains a null key or value
     * @throws IllegalArgumentException if distribution contains a negative count
     */
    public Sketch(Set<T> categories) {
      if (categories == null) {
        throw new NullPointerException();
      }
      this.categories = new HashSet<>();
      for (T category : categories) {
        if (category == null) {
          throw new NullPointerException();
        }
        this.categories.add(category);
      }
    }

    public Set<T> categories() {
      return unmodifiableSet(categories);
    }

    public void merge(Sketch<T> that) {
      categories.addAll(that.categories);
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
      categories.add(t);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Sketch<?> that)) {
        return false;
      }
      return Objects.equals(categories, that.categories);
    }

    @Override
    public int hashCode() {
      return Objects.hash(categories);
    }

    @Override
    public String toString() {
      return "Sketch{" + "categories=" + categories + '}';
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
   * @see #fit(Stream)
   */
  public static <T> Collector<T, Sketch<T>, UniformDistribution<T>> toUniformCategoricalDistribution() {
    return Collector.of(Sketch::new, Sketch::accept, (a, b) -> {
      a.merge(b);
      return a;
    }, UniformDistribution::fromSketch);
  }

  public static <T> UniformDistribution<T> fromSketch(Sketch<T> sketch) {
    return of(sketch.categories);
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
  public static <T> UniformDistribution<T> fit(Set<T> xs) {
    return fit(requireNonNull(xs).stream());
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
  public static <T> UniformDistribution<T> fit(Stream<T> stream) {
    return requireNonNull(stream).collect(toUniformCategoricalDistribution());
  }

  /**
   * Create an empirical categorical distribution from a map of elements to counts. These counts
   * determine the relative probabilities of each element. Categories with count 0 are ignored.
   *
   * @param categories a map of elements to counts
   * @param <T>        the type of the elements
   * @return a new distribution
   * @throws NullPointerException     if distribution is null or contains a null key or value
   * @throws IllegalArgumentException if distribution contains a negative count
   * @see #UniformDistribution(Set)
   */
  public static <T> UniformDistribution<T> of(Set<T> categories) {
    return new UniformDistribution<>(categories);
  }

  private final List<T> categories;

  /**
   * Create a categorical distribution from a map of elements to counts. These counts determine the
   * relative probabilities of each element. Categories with count 0 are ignored.
   *
   * @param categories a map of elements to counts
   * @throws NullPointerException     if distribution is null or contains a null key
   * @throws IllegalArgumentException if distribution is empty or contains a negative count
   */
  public UniformDistribution(Set<T> categories) {
    if (categories == null) {
      throw new NullPointerException();
    }
    if (categories.isEmpty()) {
      throw new IllegalArgumentException("distribution must not be empty");
    }
    this.categories = new ArrayList<>();
    for (T category : categories) {
      if (category == null) {
        throw new NullPointerException();
      } else {
        this.categories.add(category);
      }
    }
  }

  private UniformDistribution(List<T> categories) {
    this.categories = new ArrayList<>(categories);
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
    return categories.get(r.nextInt(categories.size()));
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
  public <U> UniformDistribution<U> map(Function<T, U> f) {
    if (f == null) {
      throw new NullPointerException();
    }
    return new UniformDistribution<>(
        categories.stream().map(x -> requireNonNull(f.apply(x))).toList());
  }

  /**
   * Returns the number of categories in the distribution.
   *
   * @return the number of elements
   */
  public int size() {
    return categories.size();
  }

  /**
   * Returns the underlying frequency data of this distribution as an unmodifiable map. The map is a
   * copy of the internal data structure, and changes to the map will not affect the distribution.
   * The stream is not guaranteed to have any particular ordering.
   *
   * @return the total number of occurrences
   */
  public Stream<T> categories() {
    return categories.stream();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UniformDistribution<?> that)) {
      return false;
    }
    if (this.categories.size() != that.categories.size()) {
      return false;
    }
    // We want the distributions to be equal even if the categories are in a different order. We
    // can't just use equals() because order is significant in List equality. We also can't sort
    // the lists because the elements are not Comparable. That leaves us with two general
    // strategies: (a) use sets, which is O(1) time complexity and O(n) space complexity,
    // or (b) iterate over list 1 and check if each element is in list 2, which is O(n^2) time
    // and O(1) space. For now, we'll use sets.
    Set thiscategories = new HashSet<>(this.categories);
    Set thatcategories = new HashSet<>(that.categories);
    if (!thiscategories.equals(thatcategories)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(categories);
  }

  @Override
  public String toString() {
    return "UniformDistribution{" + "categories=" + categories + '}';
  }
}
