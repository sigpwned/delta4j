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
import com.sigpwned.delta4j.core.statistical.distribution.categorical.CategoricalDistribution;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoricalDistributionSketchJacksonDeserializer extends
    StdDeserializer<CategoricalDistribution.Sketch<?>> implements ContextualDeserializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      CategoricalDistributionSketchJacksonDeserializer.class);

  public static final CategoricalDistributionSketchJacksonDeserializer INSTANCE = new CategoricalDistributionSketchJacksonDeserializer();

  private final JavaType categoryType;

  public CategoricalDistributionSketchJacksonDeserializer() {
    this(null);
  }

  private CategoricalDistributionSketchJacksonDeserializer(JavaType categoryType) {
    super(CategoricalDistribution.Sketch.class);
    this.categoryType = categoryType;
  }

  private static final AtomicBoolean WARNED = new AtomicBoolean(false);

  @Override
  public CategoricalDistribution.Sketch<?> deserialize(JsonParser jp,
      DeserializationContext context) throws IOException, JsonProcessingException {
    JavaType categoryType;
    if (getCategoryType() != null) {
      categoryType = getCategoryType();
    } else {
      if (LOGGER.isWarnEnabled()) {
        if (WARNED.getAndSet(true) == false) {
          LOGGER.warn(
              "While deserializing CategoricalDistribution, categoryType is unknown. Falling back to Object...");
        }
      }
      categoryType = context.constructType(Object.class);
    }

    JsonNode rootNode = jp.getCodec().readTree(jp);
    JsonNode categoriesNode = rootNode.get("categories");

    Map<Object, Long> categories = new HashMap<>();
    if (categoriesNode.isArray()) {
      for (JsonNode categoryNode : categoriesNode) {
        Object category = jp.getCodec()
            .treeToValue(categoryNode.get("category"), categoryType.getRawClass());
        long count = categoryNode.get("count").asLong();
        categories.put(category, count);
      }
    }

    // Assuming a constructor or factory method that accepts a map of categories and counts
    return new CategoricalDistribution.Sketch<>(categories);
  }

  @Override
  public JsonDeserializer<?> createContextual(DeserializationContext context,
      BeanProperty beanProperty) throws JsonMappingException {
    return new CategoricalDistributionSketchJacksonDeserializer(context.getContextualType());
  }

  private JavaType getCategoryType() {
    return categoryType;
  }
}
