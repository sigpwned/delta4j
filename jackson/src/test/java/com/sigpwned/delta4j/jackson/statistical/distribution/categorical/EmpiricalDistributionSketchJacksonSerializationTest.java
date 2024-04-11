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
package com.sigpwned.delta4j.jackson.statistical.distribution.categorical;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.JavaType;
import com.sigpwned.delta4j.core.statistical.distribution.categorical.EmpiricalDistribution;
import com.sigpwned.delta4j.jackson.JacksonSerializationTest;
import java.io.IOException;
import java.util.Map;
import org.junit.Test;

public class EmpiricalDistributionSketchJacksonSerializationTest extends
    JacksonSerializationTest {

  public static final JavaType SKETCH_TYPE = MAPPER.getTypeFactory()
      .constructParametricType(EmpiricalDistribution.Sketch.class, String.class);

  /**
   * When deserializing with a parameterized type, we should not get a warning.
   */
  @Test
  public void testSerializationAndDeserializationWithParameterizedType() throws IOException {
    EmpiricalDistribution.Sketch<String> original = new EmpiricalDistribution.Sketch<>(
        Map.of("hello", 10L, "world", 20L));

    EmpiricalDistributionSketchJacksonDeserializer.WARNED.set(false);

    EmpiricalDistribution.Sketch<String> serializedDeserialized = MAPPER.readValue(
        MAPPER.writeValueAsString(original), SKETCH_TYPE);

    assertThat(serializedDeserialized, is(original));
    assertThat(EmpiricalDistributionSketchJacksonDeserializer.WARNED.get(), is(false));
  }

  /**
   * When deserializing with a raw type, we should get a warning.
   */
  @Test
  public void testSerializationAndDeserializationWithRawType() throws IOException {
    EmpiricalDistribution.Sketch<String> original = new EmpiricalDistribution.Sketch<>(
        Map.of("hello", 10L, "world", 20L));

    EmpiricalDistributionSketchJacksonDeserializer.WARNED.set(false);

    EmpiricalDistribution.Sketch<String> serializedDeserialized = MAPPER.readValue(
        MAPPER.writeValueAsString(original), EmpiricalDistribution.Sketch.class);

    assertThat(serializedDeserialized, is(original));
    assertThat(EmpiricalDistributionSketchJacksonDeserializer.WARNED.get(), is(true));
  }
}
