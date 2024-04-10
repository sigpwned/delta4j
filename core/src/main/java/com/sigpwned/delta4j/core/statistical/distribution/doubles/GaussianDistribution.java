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
package com.sigpwned.delta4j.core.statistical.distribution.doubles;

import java.util.Objects;
import java.util.Random;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;

/**
 * A Gaussian ("normal") distribution of real numbers. The distribution is defined by a mean and a
 * standard deviation. The mean is the center of the distribution, and the standard deviation is the
 * spread of the distribution. The distribution is symmetric about the mean, and the standard
 * deviation controls the width of the distribution.
 */
public class GaussianDistribution {

  /**
   * A sketch of a stream of data for fitting a Gaussian distribution.
   */
  private static class Sketch implements DoubleConsumer {

    public double sum;
    public double sumOfSquares;
    public long count;

    public void merge(Sketch that) {
      this.sum = this.sum + that.sum;
      this.sumOfSquares = this.sumOfSquares + that.sumOfSquares;
      this.count = this.count + that.count;
    }

    @Override
    public void accept(double value) {
      this.sum = this.sum + value;
      this.sumOfSquares = this.sumOfSquares + value * value;
      this.count = this.count + 1;
    }
  }

  /**
   * Fits a Gaussian distribution to the specified stream of data.
   *
   * @param stream the stream of data
   * @return an optional Gaussian distribution
   * @throws IllegalArgumentException if the stream contains less than two elements
   * @throws IllegalArgumentException if the stream elements are all the same
   */
  public static GaussianDistribution fit(DoubleStream stream) {
    Sketch sketch = stream.collect(Sketch::new, Sketch::accept, Sketch::merge);
    if (sketch.count < 2) {
      // No data to fit
      throw new IllegalArgumentException("insufficient data");
    }
    double mu = sketch.sum / sketch.count;
    double sigma = Math.sqrt(sketch.sumOfSquares / sketch.count - mu * mu);
    if (sigma == 0.0) {
      // No variance to fit
      throw new IllegalArgumentException("no variance");
    }
    return new GaussianDistribution(mu, sigma);
  }

  /**
   * Returns a Gaussian distribution with the specified mean and standard deviation.
   *
   * @param mu    the mean
   * @param sigma the standard deviation
   * @return a Gaussian distribution
   * @throws IllegalArgumentException if {@code sigma} is less than or equal to zero
   */
  public static GaussianDistribution of(double mu, double sigma) {
    return new GaussianDistribution(mu, sigma);
  }

  private final double mu;
  private final double sigma;

  /**
   * Constructs a Gaussian distribution with the specified mean and standard deviation.
   *
   * @param mu    the mean
   * @param sigma the standard deviation
   * @throws IllegalArgumentException if {@code sigma} is less than or equal to zero
   */
  public GaussianDistribution(double mu, double sigma) {
    if (sigma == 0.0) {
      throw new IllegalArgumentException("sigma must not be zero");
    }
    if (sigma < 0.0) {
      throw new IllegalArgumentException("sigma must be positive");
    }
    this.mu = mu;
    this.sigma = sigma;
  }

  /**
   * Returns the mean of this distribution.
   *
   * @return the mean
   */
  public double mu() {
    return mu;
  }

  /**
   * Returns the standard deviation of this distribution.
   *
   * @return the standard deviation
   */
  public double sigma() {
    return sigma;
  }

  /**
   * Samples a random value from this distribution.
   *
   * @param rand the random number generator
   * @return a random value from this distribution
   */
  public double sample(Random rand) {
    return mu + sigma * rand.nextGaussian();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GaussianDistribution that)) {
      return false;
    }
    return Double.compare(mu, that.mu) == 0 && Double.compare(sigma, that.sigma) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(mu, sigma);
  }

  @Override
  public String toString() {
    return "GaussianDistribution{" + "mu=" + mu + ", sigma=" + sigma + '}';
  }
}
