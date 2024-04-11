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

import java.util.Random;
import java.util.function.Function;

/**
 * A distribution of objects.
 *
 * @param <T> the type of the objects
 */
public interface CategoricalDistribution<T> {

  /**
   * Samples a value from the distribution.
   *
   * @param random the random number generator
   * @return a value sampled from the distribution
   */
  public T sample(Random random);

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
  public <U> CategoricalDistribution<U> map(Function<T, U> f);
}
