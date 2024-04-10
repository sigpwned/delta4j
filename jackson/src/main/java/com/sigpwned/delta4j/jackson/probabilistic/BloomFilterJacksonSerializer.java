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
package com.sigpwned.delta4j.jackson.probabilistic;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.sigpwned.delta4j.core.probabilistic.BloomFilter;
import java.io.IOException;

public class BloomFilterJacksonSerializer extends JsonSerializer<BloomFilter<?>> {

  public static final BloomFilterJacksonSerializer INSTANCE = new BloomFilterJacksonSerializer();

  @Override
  public void serialize(BloomFilter<?> value, JsonGenerator g, SerializerProvider p)
      throws IOException {
    g.writeStartObject();
    g.writeNumberField("expectedSize", value.getExpectedSize());
    g.writeNumberField("falsePositiveProbability", value.getFalsePositiveProbability());
    g.writeBinaryField("bits", value.toByteArray());
    g.writeEndObject();
  }
}
