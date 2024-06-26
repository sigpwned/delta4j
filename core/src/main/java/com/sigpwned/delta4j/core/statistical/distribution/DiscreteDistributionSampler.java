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
package com.sigpwned.delta4j.core.statistical.distribution;

import static java.util.Objects.requireNonNull;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * Samples objects from a discrete distribution.
 */
public class DiscreteDistributionSampler implements LongSupplier {

  public static DiscreteDistributionSampler ofInstance(DiscreteDistribution distribution,
      Random rand) {
    return of(distribution, () -> rand);
  }

  public static DiscreteDistributionSampler ofThreadLocal(DiscreteDistribution distribution) {
    return of(distribution, ThreadLocalRandom::current);
  }

  public static DiscreteDistributionSampler of(DiscreteDistribution distribution,
      Supplier<Random> randomSupplier) {
    return new DiscreteDistributionSampler(distribution, randomSupplier);
  }

  private final DiscreteDistribution distribution;
  private final Supplier<Random> randomSupplier;

  /**
   * Constructs an object distribution sampler.
   *
   * @param distribution   the object distribution
   * @param randomSupplier the random number generator supplier
   */
  public DiscreteDistributionSampler(DiscreteDistribution distribution,
      Supplier<Random> randomSupplier) {
    this.distribution = requireNonNull(distribution);
    this.randomSupplier = requireNonNull(randomSupplier);
  }

  /**
   * Returns a value sampled from the distribution.
   *
   * @return a value sampled from the distribution
   */
  public long sample() {
    return distribution.sample(randomSupplier.get());
  }

  /**
   * Returns a value sampled from the distribution. Equivalent to {@link #sample() sample()}.
   *
   * @return a value sampled from the distribution
   * @see #sample()
   */
  @Override
  public long getAsLong() {
    return sample();
  }
}
