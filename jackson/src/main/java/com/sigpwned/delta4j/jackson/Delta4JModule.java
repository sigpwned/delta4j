/*-
 * =================================LICENSE_START==================================
 * delta4j-jackson
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
package com.sigpwned.delta4j.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sigpwned.delta4j.core.probabilistic.BloomFilter;
import com.sigpwned.delta4j.core.statistical.distribution.categorical.EmpiricalDistribution;
import com.sigpwned.delta4j.core.statistical.distribution.categorical.UniformDistribution;
import com.sigpwned.delta4j.core.statistical.distribution.continuous.GaussianDistribution;
import com.sigpwned.delta4j.jackson.probabilistic.BloomFilterJacksonDeserializer;
import com.sigpwned.delta4j.jackson.probabilistic.BloomFilterJacksonSerializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.categorical.EmpiricalDistributionJacksonDeserializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.categorical.EmpiricalDistributionJacksonSerializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.categorical.EmpiricalDistributionSketchJacksonDeserializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.categorical.EmpiricalDistributionSketchJacksonSerializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.categorical.UniformDistributionJacksonDeserializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.categorical.UniformDistributionJacksonSerializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.categorical.UniformDistributionSketchJacksonDeserializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.categorical.UniformDistributionSketchJacksonSerializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.continuous.GaussianDistributionJacksonDeserializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.continuous.GaussianDistributionJacksonSerializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.continuous.GaussianDistributionSketchJacksonDeserializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.continuous.GaussianDistributionSketchJacksonSerializer;

public class Delta4JModule extends SimpleModule {

  @Override
  public String getModuleName() {
    return "delta4j";
  }

  @Override
  public void setupModule(SetupContext context) {
    // BloomFilter /////////////////////////////////////////////////////////////////////////////////
    addSerializer(BloomFilterJacksonSerializer.INSTANCE);
    addDeserializer(BloomFilter.class, BloomFilterJacksonDeserializer.INSTANCE);

    // GaussianDistribution ////////////////////////////////////////////////////////////////////////
    addSerializer(GaussianDistributionJacksonSerializer.INSTANCE);
    addDeserializer(GaussianDistribution.class, GaussianDistributionJacksonDeserializer.INSTANCE);
    addSerializer(GaussianDistributionSketchJacksonSerializer.INSTANCE);
    addDeserializer(GaussianDistribution.Sketch.class,
        GaussianDistributionSketchJacksonDeserializer.INSTANCE);

    // EmpiricalDistribution ///////////////////////////////////////////////////////////////////////
    addSerializer(EmpiricalDistributionJacksonSerializer.INSTANCE);
    addDeserializer(EmpiricalDistribution.class, EmpiricalDistributionJacksonDeserializer.INSTANCE);
    addSerializer(EmpiricalDistributionSketchJacksonSerializer.INSTANCE);
    addDeserializer(EmpiricalDistribution.Sketch.class,
        EmpiricalDistributionSketchJacksonDeserializer.INSTANCE);

    // UniformDistribution /////////////////////////////////////////////////////////////////////////
    addSerializer(UniformDistributionJacksonSerializer.INSTANCE);
    addDeserializer(UniformDistribution.class, UniformDistributionJacksonDeserializer.INSTANCE);
    addSerializer(UniformDistributionSketchJacksonSerializer.INSTANCE);
    addDeserializer(UniformDistribution.Sketch.class,
        UniformDistributionSketchJacksonDeserializer.INSTANCE);

    super.setupModule(context);
  }
}
