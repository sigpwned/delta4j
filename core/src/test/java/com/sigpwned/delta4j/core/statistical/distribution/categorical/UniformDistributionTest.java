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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

public class UniformDistributionTest {

  @Test
  public void shouldSampleCorrectly() {
    UniformDistribution<String> distribution = UniformDistribution.fit(singleton("test"));
    assertThat(distribution.sample(new Random()), is("test"));
  }

  @Test
  public void shouldHandleMultipleCategories() {
    Set<String> categories = new HashSet<>();
    categories.add("test1");
    categories.add("test2");
    UniformDistribution<String> distribution = UniformDistribution.fit(categories);
    assertThat(distribution.size(), is(2));
  }

  @Test
  public void shouldMapCorrectly() {
    UniformDistribution<String> distribution = UniformDistribution.fit(singleton("test"));
    UniformDistribution<Integer> mapped = distribution.map(String::length);
    assertThat(mapped.sample(new Random()), is(4));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldHandleEmptyDistribution() {
    UniformDistribution.of(emptySet());
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowExceptionForNullCategory() {
    UniformDistribution.fit(singleton(null));
  }
}
