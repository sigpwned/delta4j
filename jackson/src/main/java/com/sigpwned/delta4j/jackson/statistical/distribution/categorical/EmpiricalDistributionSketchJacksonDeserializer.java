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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.sigpwned.delta4j.core.statistical.distribution.categorical.EmpiricalDistribution;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmpiricalDistributionSketchJacksonDeserializer extends
    StdDeserializer<EmpiricalDistribution.Sketch<?>> implements ContextualDeserializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      EmpiricalDistributionSketchJacksonDeserializer.class);

  public static final EmpiricalDistributionSketchJacksonDeserializer INSTANCE = new EmpiricalDistributionSketchJacksonDeserializer();

  private final JavaType contextualType;

  public EmpiricalDistributionSketchJacksonDeserializer() {
    this(null);
  }

  private EmpiricalDistributionSketchJacksonDeserializer(JavaType contextualType) {
    super(EmpiricalDistribution.Sketch.class);
    this.contextualType = contextualType;
  }

  /* default */ static final AtomicBoolean WARNED = new AtomicBoolean(false);

  @Override
  public EmpiricalDistribution.Sketch<?> deserialize(JsonParser p, DeserializationContext context)
      throws IOException, JsonProcessingException {
    JavaType categoryType = null;
    if (categoryType == null && contextualType != null) {
      categoryType = contextualType.containedType(0);
    }
    if (categoryType == null) {
      if (LOGGER.isWarnEnabled()) {
        if (WARNED.getAndSet(true) == false) {
          LOGGER.warn(
              "While deserializing EmpiricalDistribution.Sketch, categoryType is unknown. Falling back to Object...");
        }
      }
      categoryType = context.constructType(Object.class);
    }

    JsonNode rootNode = p.getCodec().readTree(p);

    JsonNode categoriesNode = rootNode.get("categories");

    Map<Object, Long> categories = new HashMap<>();
    if (categoriesNode.isArray()) {
      for (JsonNode categoryNode : categoriesNode) {
        Object category = p.getCodec()
            .treeToValue(categoryNode.get("category"), categoryType.getRawClass());
        long count = categoryNode.get("count").asLong();
        categories.put(category, count);
      }
    }

    // Assuming a constructor or factory method that accepts a map of categories and counts
    return new EmpiricalDistribution.Sketch<>(categories);
  }

  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext context,
      BeanProperty beanProperty) throws JsonMappingException {
    return new EmpiricalDistributionSketchJacksonDeserializer(context.getContextualType());
  }
}
