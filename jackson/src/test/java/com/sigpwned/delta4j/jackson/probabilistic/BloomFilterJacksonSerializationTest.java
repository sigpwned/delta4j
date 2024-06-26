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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.sigpwned.delta4j.core.probabilistic.BloomFilter;
import com.sigpwned.delta4j.jackson.JacksonSerializationTest;
import java.io.IOException;
import org.junit.Test;

public class BloomFilterJacksonSerializationTest extends JacksonSerializationTest {

  @Test
  public void testSerializationAndDeserialization() throws IOException {
    BloomFilter<?> original = new BloomFilter<>(1000, 0.01);

    BloomFilter<?> serializedDeserialized = MAPPER.readValue(
        MAPPER.writeValueAsString(original), BloomFilter.class);

    assertThat(serializedDeserialized, is(original));
  }
}
