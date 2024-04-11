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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.sigpwned.delta4j.core.statistical.distribution.continuous.GaussianDistribution;
import com.sigpwned.delta4j.jackson.JacksonSerializationTest;
import java.io.IOException;
import org.junit.Test;

public class GaussianDistributionSketchJacksonSerializationTest extends JacksonSerializationTest {

  @Test
  public void testSerializationAndDeserialization() throws IOException {
    GaussianDistribution.Sketch original = new GaussianDistribution.Sketch(10.0, 20.0, 5L);

    GaussianDistribution.Sketch serializedDeserialized = MAPPER.readValue(
        MAPPER.writeValueAsString(original), GaussianDistribution.Sketch.class);

    assertThat(serializedDeserialized, is(original));
  }
}
