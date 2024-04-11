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

import java.util.Map;
import java.util.Random;
import org.junit.Test;

public class EmpiricalDistributionTest {

  @Test
  public void shouldSampleCorrectly() {
    EmpiricalDistribution<String> distribution = EmpiricalDistribution.of(singletonMap("test", 1L));
    assertThat(distribution.sample(new Random()), is("test"));
  }

  @Test
  public void shouldHandleMultipleCategories() {
    EmpiricalDistribution<String> distribution = EmpiricalDistribution.of(
        Map.of("test1", 1L, "test2", 1L));
    assertThat(distribution.size(), is(2));
  }

  @Test
  public void shouldIgnoreZeroCountCategories() {
    EmpiricalDistribution<String> distribution = EmpiricalDistribution.of(
        Map.of("test1", 1L, "test2", 0L));
    assertThat(distribution.size(), is(1));
  }

  @Test
  public void shouldMapCorrectly() {
    EmpiricalDistribution<String> distribution = EmpiricalDistribution.of(singletonMap("test", 1L));
    EmpiricalDistribution<Integer> mapped = distribution.map(String::length);
    assertThat(mapped.sample(new Random()), is(4));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldHandleEmptyDistribution() {
    EmpiricalDistribution.of(Map.of());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionForNegativeCount() {
    EmpiricalDistribution.of(singletonMap("test", -1L));
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowExceptionForNullCategory() {
    EmpiricalDistribution.of(singletonMap(null, 1L));
  }
}
