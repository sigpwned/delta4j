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
package com.sigpwned.delta4j.jackson.statistical.distribution.continuous;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.sigpwned.delta4j.core.statistical.distribution.continuous.GaussianDistribution;
import java.io.IOException;

public class GaussianDistributionJacksonDeserializer extends
    JsonDeserializer<GaussianDistribution> {

  @Override
  public GaussianDistribution deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {

    JsonNode node = jp.getCodec().readTree(jp);
    // Extract the mean (mu) and standard deviation (sigma) from the JSON object
    double mu = node.get("mu").asDouble();
    double sigma = node.get("sigma").asDouble();

    return new GaussianDistribution(mu, sigma);
  }
}