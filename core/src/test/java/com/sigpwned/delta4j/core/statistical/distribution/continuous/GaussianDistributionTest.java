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
package com.sigpwned.delta4j.core.statistical.distribution.continuous;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import org.junit.Test;

public class GaussianDistributionTest {

  @Test
  public void shouldCreateDistributionWithGivenParameters() {
    GaussianDistribution distribution = GaussianDistribution.of(0.0, 1.0);
    assertThat(distribution.mu(), is(0.0));
    assertThat(distribution.sigma(), is(1.0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionForZeroSigma() {
    GaussianDistribution.of(0.0, 0.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionForNegativeSigma() {
    GaussianDistribution.of(0.0, -1.0);
  }

  @Test
  public void shouldSampleFromDistribution() {
    GaussianDistribution distribution = GaussianDistribution.of(0.0, 1.0);
    double sample = distribution.sample(new Random());
    assertTrue(sample >= -5.0 && sample <= 5.0);
  }

  @Test
  public void shouldCreateDistributionFromSketch() {
    GaussianDistribution.Sketch sketch = new GaussianDistribution.Sketch();
    sketch.accept(1.0);
    sketch.accept(2.0);
    sketch.accept(3.0);
    GaussianDistribution distribution = GaussianDistribution.fromSketch(sketch);
    assertEquals(2.0, distribution.mu(), 0.01);
    assertEquals(Math.sqrt(2.0 / 3.0), distribution.sigma(), 0.01);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionForInsufficientDataInSketch() {
    GaussianDistribution.Sketch sketch = new GaussianDistribution.Sketch();
    sketch.accept(1.0);
    GaussianDistribution.fromSketch(sketch);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionForNoVarianceInSketch() {
    GaussianDistribution.Sketch sketch = new GaussianDistribution.Sketch();
    sketch.accept(1.0);
    sketch.accept(1.0);
    GaussianDistribution.fromSketch(sketch);
  }
}
