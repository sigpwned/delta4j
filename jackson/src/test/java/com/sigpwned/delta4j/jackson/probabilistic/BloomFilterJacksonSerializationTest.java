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

import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigpwned.delta4j.core.probabilistic.BloomFilter;
import com.sigpwned.delta4j.jackson.Delta4JModule;
import java.io.IOException;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class BloomFilterJacksonSerializationTest {

  @Test
  public void testSerializationAndDeserialization() throws IOException {
    // Create a new BloomFilter
    BloomFilter<?> originalBloomFilter = new BloomFilter<>(1000, 0.01);

    // Create ObjectMapper and register the BloomFilterJacksonSerializer and BloomFilterJacksonDeserializer
    ObjectMapper mapper = new ObjectMapper().registerModule(new Delta4JModule());

    // Serialize the BloomFilter
    String json = mapper.writeValueAsString(originalBloomFilter);

    // Deserialize the BloomFilter
    BloomFilter<?> deserializedBloomFilter = mapper.readValue(json, BloomFilter.class);

    // Confirm that the new BloomFilter equals the original BloomFilter
    assertThat(originalBloomFilter, CoreMatchers.is(deserializedBloomFilter));
  }
}
