/*-
 * =================================LICENSE_START==================================
 * core
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
package com.sigpwned.delta4j.core.probabilistic;

import static java.util.Objects.requireNonNull;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * <p>
 * A Bloom filter is a probabilistic data structure that can test whether an element is a member of
 * a set. It can return false positives, but never false negatives. The likelihood of a false
 * positive is parametric and can be controlled by the user.
 * </p>
 *
 * @param <T> the type of elements to store in the Bloom filter
 */
public class BloomFilter<T> {

  @FunctionalInterface
  public static interface HashFunction {

    public int hash(int hashCode);
  }

  public static final double DEFAULT_FALSE_POSITIVE_RATE = 1.0 / 1_000.0;

  public static final double MIN_FALSE_POSITIVE_RATE = 1.0 / 1_000_000.0;

  /**
   * The first 20 prime numbers. These are used as the basis for the hash functions. Because the
   * formula for the number of hash functions is -ln(false positive rate) / ln(2), 20 hash functions
   * are sufficient to achieve a false positive rate of 1 in 1,048,576.
   */
  private static final int[] PRIMES = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53,
      59, 61, 67, 71};

  /**
   * Generate the numbered default hash function. The given index is a number starting from 0. It's
   * important that all Bloom filters use the same hash functions in the same order. This method can
   * generate sufficient hash functions to satsify {@link #MIN_FALSE_POSITIVE_RATE} only.
   *
   * @param index the index of the hash function to generate
   * @return the hash function
   */
  public static HashFunction generateHashFunction(int index) {
    if (index < 0 || index >= PRIMES.length) {
      throw new IllegalArgumentException("index must be in the range [0, " + PRIMES.length + ")");
    }
    return hc -> hc + PRIMES[index] * murmur3(hc);
  }

  private static final double LN_OF_2 = Math.log(2);

  /**
   * Compute the optimal number of hash functions for a Bloom filter given the desired false
   * positive probability.
   *
   * @param falsePositiveProbability the desired false positive probability
   * @return the optimal number of hash functions
   * @see <a href="https://en.wikipedia.org/wiki/Bloom_filter#Optimal_number_of_hash_functions">
   * https://en.wikipedia.org/wiki/Bloom_filter#Optimal_number_of_hash_functions</a>
   */
  public static int optimalHashFunctions(double falsePositiveProbability) {
    if (falsePositiveProbability <= 0.0 || falsePositiveProbability >= 1.0) {
      throw new IllegalArgumentException("falsePositiveRate must be in the range (0, 1)");
    }
    if (falsePositiveProbability < MIN_FALSE_POSITIVE_RATE) {
      throw new IllegalArgumentException(
          "falsePositiveRate must be greater than or equal to MIN_FALSE_POSITIVE_RATE");
    }
    return (int) Math.ceil(-Math.log(falsePositiveProbability) / LN_OF_2);
  }

  private static final double LN_OF_2_SQUARED = LN_OF_2 * LN_OF_2;

  /**
   * Compute the optimal number of bits for a Bloom filter given the expected size and false
   * positive probability.
   *
   * @param expectedSize             the expected number of elements
   * @param falsePositiveProbability the desired false positive probability
   * @return the optimal number of bits
   */
  public static int optimalBits(long expectedSize, double falsePositiveProbability) {
    return (int) Math.ceil(-expectedSize * Math.log(falsePositiveProbability) / LN_OF_2_SQUARED);
  }

  /**
   * Returns a new Bloom filter to which all the elements in the given stream have been added. The
   * expected size of the Bloom filter is the number of elements in the stream. The false positive
   * probability is {@link #DEFAULT_FALSE_POSITIVE_RATE the default}.
   *
   * @param streamer The supplier of the stream of elements to add to the Bloom filter. The stream
   *                 must contain the same elements each time it is consumed.
   * @see #fit(Supplier, double)
   */
  public static <T> BloomFilter<T> fit(Supplier<Stream<T>> streamer) {
    return fit(streamer, DEFAULT_FALSE_POSITIVE_RATE);
  }

  /**
   * Returns a new Bloom filter to which all the elements in the given stream have been added. The
   * expected size of the Bloom filter is the number of elements in the stream.
   *
   * @param streamer                 The supplier of the stream of elements to add to the Bloom
   *                                 filter. The stream must contain the same elements each time it
   *                                 is consumed.
   * @param falsePositiveProbability the desired false positive probability
   * @see #fit(Supplier, long, double)
   */
  public static <T> BloomFilter<T> fit(Supplier<Stream<T>> streamer,
      double falsePositiveProbability) {
    final long expectedSize;
    try (Stream<T> stream = streamer.get()) {
      expectedSize = stream.count();
    }
    return fit(streamer, expectedSize, falsePositiveProbability);
  }

  /**
   * Returns a new Bloom filter to which all the elements in the given stream have been added. The
   * expected size of the Bloom filter is the number of elements in the stream.
   *
   * @param streamer                 The supplier of the stream of elements to add to the Bloom
   *                                 filter. The stream must contain the same elements each time it
   *                                 is consumed.
   * @param falsePositiveProbability the desired false positive probability
   * @see #fit(Stream, long, double)
   */
  public static <T> BloomFilter<T> fit(Supplier<Stream<T>> streamer, long expectedSize,
      double falsePositiveProbability) {
    try (Stream<T> stream = streamer.get()) {
      return fit(stream, expectedSize, falsePositiveProbability);
    }
  }

  /**
   * Returns a new Bloom filter to which all the elements in the given stream have been added.
   *
   * @param stream                   The stream of elements to add to the Bloom filter.
   * @param expectedSize             The expected number of elements to add to the Bloom filter.
   *                                 This number is not checked against the actual number of
   *                                 elements in stream.
   * @param falsePositiveProbability the desired false positive probability
   */
  public static <T> BloomFilter<T> fit(Stream<T> stream, long expectedSize,
      double falsePositiveProbability) {
    return stream.collect(toBloomFilter(expectedSize, falsePositiveProbability));
  }

  /**
   * Equivalent to
   * {@link #toBloomFilter(long, double) toBloomFilter(expectedSize, DEFAULT_FALSE_POSITIVE_RATE)}
   *
   * @see #DEFAULT_FALSE_POSITIVE_RATE
   */
  public static <T> Collector<T, ?, BloomFilter<T>> toBloomFilter(long expectedSize) {
    return toBloomFilter(expectedSize, DEFAULT_FALSE_POSITIVE_RATE);
  }

  /**
   * Create a {@link Collector} that collects elements into a {@link BloomFilter}. The expected size
   * of the Bloom filter and the desired false positive probability must be specified. The returned
   * collector is suitable for use with parallel or sequential streams.
   *
   * @param expectedSize             the expected number of elements to add to the Bloom filter
   * @param falsePositiveProbability the desired false positive probability
   * @param <T>                      the type of elements to store in the Bloom filter
   * @return a collector that collects elements into a Bloom filter
   */
  public static <T> Collector<T, ?, BloomFilter<T>> toBloomFilter(long expectedSize,
      double falsePositiveProbability) {
    return Collector.of(() -> new BloomFilter<>(expectedSize, falsePositiveProbability),
        BloomFilter::add, (a, b) -> {
          a.merge(b);
          return a;
        });
  }

  private final long expectedSize;
  private final double falsePositiveProbability;
  private final BitSet bits;
  private final List<HashFunction> hashFunctions;
  private final transient int numBits;
  private final transient int numHashFunctions;
  private transient Integer approximateSize;

  /**
   * Equivalent to
   * {@link #BloomFilter(long, double) BloomFilter(expectedSize, DEFAULT_FALSE_POSITIVE_RATE)}.
   *
   * @param expectedSize the expected number of elements to add to this bloom filter
   * @see #DEFAULT_FALSE_POSITIVE_RATE
   */
  public BloomFilter(long expectedSize) {
    this(expectedSize, DEFAULT_FALSE_POSITIVE_RATE);
  }

  /**
   * Create a Bloom filter with the given expected size and false positive probability. The number
   * of bits and the number of hash functions in the filter are computed automatically based on
   * these parameters. The hash functions are generated using the default hash function generator.
   *
   * @param expectedSize             the expected number of elements to add to this bloom filter
   * @param falsePositiveProbability the desired false positive probability
   * @see #optimalBits(long, double)
   * @see #optimalHashFunctions(double)
   * @see #generateHashFunction(int)
   */
  public BloomFilter(long expectedSize, double falsePositiveProbability) {
    if (expectedSize <= 0) {
      throw new IllegalArgumentException("size must be positive");
    }
    if (falsePositiveProbability <= 0.0 || falsePositiveProbability >= 1.0) {
      throw new IllegalArgumentException("falsePositiveRate must be in the range (0, 1)");
    }
    if (falsePositiveProbability < MIN_FALSE_POSITIVE_RATE) {
      throw new IllegalArgumentException(
          "falsePositiveRate must be greater than or equal to MIN_FALSE_POSITIVE_RATE");
    }
    this.expectedSize = expectedSize;
    this.falsePositiveProbability = falsePositiveProbability;
    this.numBits = optimalBits(expectedSize, falsePositiveProbability);
    this.bits = new BitSet(numBits);
    this.numHashFunctions = optimalHashFunctions(falsePositiveProbability);
    this.hashFunctions = IntStream.range(0, numHashFunctions)
        .mapToObj(BloomFilter::generateHashFunction).toList();
  }

  /**
   * Create a Bloom filter with the given expected size, false positive probability, and bit data.
   * The number of bits and the number of hash functions in the filter are computed automatically
   * based on the expected size and false positive probability. The hash functions are generated
   * using the default hash function generator. The bit data should have been retrieved from an
   * existing Bloom filter using {@link #toByteArray()}.
   *
   * @param expectedSize             the expected number of elements to add to this bloom filter
   * @param falsePositiveProbability the desired false positive probability
   * @param bits                     the bit data of the Bloom filter
   * @see #optimalBits(long, double)
   * @see #optimalHashFunctions(double)
   * @see #generateHashFunction(int)
   */
  public BloomFilter(long expectedSize, double falsePositiveProbability, byte[] bits) {
    if (expectedSize <= 0) {
      throw new IllegalArgumentException("size must be positive");
    }
    if (falsePositiveProbability <= 0.0 || falsePositiveProbability >= 1.0) {
      throw new IllegalArgumentException("falsePositiveRate must be in the range (0, 1)");
    }
    if (falsePositiveProbability < MIN_FALSE_POSITIVE_RATE) {
      throw new IllegalArgumentException(
          "falsePositiveRate must be greater than or equal to MIN_FALSE_POSITIVE_RATE");
    }
    if (bits == null) {
      throw new NullPointerException();
    }
    this.expectedSize = expectedSize;
    this.falsePositiveProbability = falsePositiveProbability;
    this.numBits = optimalBits(expectedSize, falsePositiveProbability);
    if (bits.length * 8 > numBits) {
      throw new IllegalArgumentException("bits must have maximum length " + numBits / 8);
    }
    this.bits = BitSet.valueOf(bits);
    this.numHashFunctions = optimalHashFunctions(falsePositiveProbability);
    this.hashFunctions = IntStream.range(0, numHashFunctions)
        .mapToObj(BloomFilter::generateHashFunction).toList();
  }

  /**
   * Add a value to the Bloom filter.
   *
   * @param value the value to add
   */
  public void add(T value) {
    value = requireNonNull(value);
    final int hashCode = value.hashCode();
    for (HashFunction hashFunction : hashFunctions) {
      bits.set(getBitIndex(hashFunction, hashCode));
    }
    approximateSize = null;
  }

  /**
   * Add all the values in the given collection to the Bloom filter.
   *
   * @param values the values to add
   */
  public void addAll(Collection<? extends T> values) {
    for (T value : values) {
      add(value);
    }
  }

  /**
   * Test whether the Bloom filter might contain a value. Remember that false positives are
   * possible. So if this method returns false, the value definitely is not in the Bloom filter, but
   * if it returns true, the value might be in the Bloom filter.
   *
   * @param value the value to test
   * @return true if the Bloom filter might contain the value, false if it definitely does not
   */
  public boolean mightContain(T value) {
    value = requireNonNull(value);
    final int hashCode = value.hashCode();
    for (HashFunction hashFunction : hashFunctions) {
      if (!bits.get(getBitIndex(hashFunction, hashCode))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Given a hash function and a hash code, compute the index of the bit in the Bloom filter that
   * corresponds to the hash code.
   *
   * @param hashFunction the hash function
   * @param hashCode     the value hash code
   * @return the index of the bit in the Bloom filter that corresponds to the hash code
   */
  private int getBitIndex(HashFunction hashFunction, int hashCode) {
    return Math.abs(hashFunction.hash(hashCode)) % numBits;
  }

  /**
   * All all the values in the given Bloom filter to this Bloom filter. The two Bloom filters must
   * have the same number of bits and the same number of hash functions.
   *
   * @param that the other Bloom filter
   * @throws IllegalArgumentException if the two Bloom filters do not have the same number of bits
   *                                  or the same number of hash functions
   */
  public void merge(BloomFilter<T> that) {
    if (this.numBits != that.numBits) {
      throw new IllegalArgumentException("Bloom filters must have the same number of bits");
    }
    if (this.numHashFunctions != that.numHashFunctions) {
      throw new IllegalArgumentException(
          "Bloom filters must have the same number of hash functions");
    }
    this.bits.or(that.bits);
    this.approximateSize = null;
  }

  /**
   * Returns true if this Bloom filter contains no elements, and false otherwise. This test is
   * equivalent to but more efficient than {@code approximateSize() == 0}.
   *
   * @return true if this Bloom filter contains no elements, and false otherwise
   */
  public boolean isEmpty() {
    return bits.isEmpty();
  }

  /**
   * Compute an approximation of the number of elements that have been added to the Bloom filter.
   *
   * @return an approximation of the number of elements that have been added to the Bloom filter
   * @see <a
   * href="https://en.wikipedia.org/wiki/Bloom_filter#Approximating_the_number_of_items_in_a_Bloom_filter">
   * https://en.wikipedia.org/wiki/Bloom_filter#Approximating_the_number_of_items_in_a_Bloom_filter</a>
   */
  public int approximateSize() {
    if (approximateSize == null) {
      double numBits = (double) this.numBits;
      double numHashFunctions = (double) this.numHashFunctions;
      approximateSize = (int) Math.ceil(
          -numBits / numHashFunctions * Math.log(1.0 - bits.cardinality() / numBits));
    }
    return approximateSize;
  }

  /**
   * Returns the expected number of elements that will be added to this Bloom filter. This is equal
   * to the expected size given at object creation.
   *
   * @return the expected number of elements that have been added to the Bloom filter
   */
  public long getExpectedSize() {
    return expectedSize;
  }

  /**
   * Returns the false positive probability of this Bloom filter. This is equal to the false
   * positive probability given at object creation.
   *
   * @return the false positive probability of this Bloom filter
   */
  public double getFalsePositiveProbability() {
    return falsePositiveProbability;
  }

  /**
   * Returns the current bit data of the Bloom filter as a byte array. This is useful for storing
   * the Bloom filter in a database or sending it over a network. This is a copy of the internal bit
   * data, so changes to the returned byte array will not be reflected in the Bloom filter.
   *
   * @return the current bit data of the Bloom filter as a byte array
   */
  public byte[] toByteArray() {
    return bits.toByteArray();
  }

  /**
   * The seed for the MurmurHash3 hash functions. It is the 1,000th prime number.
   */
  private static final int SEED = 7919;

  /**
   * Compute the MurmurHash3 hash of the given 32-bit integer using the given seed.
   *
   * @param input the input integer
   * @return the hash
   */
  private static int murmur3(final int input) {
    int c1 = 0xcc9e2d51;
    int c2 = 0x1b873593;

    int k1 = input;
    k1 *= c1;
    k1 = Integer.rotateLeft(k1, 15);
    k1 *= c2;

    int h1 = SEED;
    h1 ^= k1;
    h1 = Integer.rotateLeft(h1, 13);
    h1 = h1 * 5 + 0xe6546b64;

    // Finalization
    h1 ^= 4; // Since we're hashing a single 32-bit int, the length is 4 bytes

    // fmix32
    h1 ^= h1 >>> 16;
    h1 *= 0x85ebca6b;
    h1 ^= h1 >>> 13;
    h1 *= 0xc2b2ae35;
    h1 ^= h1 >>> 16;

    return h1;
  }
}
