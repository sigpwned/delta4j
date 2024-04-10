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
import com.sigpwned.delta4j.core.statistical.distribution.categorical.CategoricalDistribution;
import com.sigpwned.delta4j.core.statistical.distribution.continuous.GaussianDistribution;
import com.sigpwned.delta4j.jackson.probabilistic.BloomFilterJacksonDeserializer;
import com.sigpwned.delta4j.jackson.probabilistic.BloomFilterJacksonSerializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.categorical.CategoricalDistributionJacksonDeserializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.categorical.CategoricalDistributionJacksonSerializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.categorical.CategoricalDistributionSketchJacksonDeserializer;
import com.sigpwned.delta4j.jackson.statistical.distribution.categorical.CategoricalDistributionSketchJacksonSerializer;
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
    addSerializer(BloomFilterJacksonSerializer.INSTANCE);
    addDeserializer(BloomFilter.class, BloomFilterJacksonDeserializer.INSTANCE);

    addSerializer(GaussianDistributionJacksonSerializer.INSTANCE);
    addDeserializer(GaussianDistribution.class, GaussianDistributionJacksonDeserializer.INSTANCE);
    addSerializer(GaussianDistributionSketchJacksonSerializer.INSTANCE);
    addDeserializer(GaussianDistribution.Sketch.class,
        GaussianDistributionSketchJacksonDeserializer.INSTANCE);

    addSerializer(CategoricalDistributionJacksonSerializer.INSTANCE);
    addDeserializer(CategoricalDistribution.class,
        CategoricalDistributionJacksonDeserializer.INSTANCE);
    addSerializer(CategoricalDistributionSketchJacksonSerializer.INSTANCE);
    addDeserializer(CategoricalDistribution.Sketch.class,
        CategoricalDistributionSketchJacksonDeserializer.INSTANCE);

    super.setupModule(context);
  }
}
