/*-
 * =================================LICENSE_START==================================
 * delta4j-core
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


package com.sigpwned.delta4j.core.probabilistic;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;
import org.junit.Test;

public class BloomFilterTest {

  @Test
  public void shouldAddAndContainElement() {
    BloomFilter<String> bloomFilter = new BloomFilter<>(100);
    bloomFilter.add("test");
    assertTrue(bloomFilter.mightContain("test"));
  }

  @Test
  public void shouldNotContainNonExistentElement() {
    BloomFilter<String> bloomFilter = new BloomFilter<>(100);
    bloomFilter.add("test");
    assertFalse(bloomFilter.mightContain("nonexistent"));
  }

  @Test
  public void shouldHandleFalsePositiveRate() {
    BloomFilter<String> bloomFilter = new BloomFilter<>(100, 0.1);
    bloomFilter.add("test");
    assertEquals(0.1, bloomFilter.getFalsePositiveProbability(), 1e-6);
  }

  @Test
  public void shouldThrowExceptionForInvalidFalsePositiveRate() {
    assertThrows(IllegalArgumentException.class, () -> new BloomFilter<>(100, -0.1));
    assertThrows(IllegalArgumentException.class, () -> new BloomFilter<>(100, 1.1));
  }

  @Test
  public void shouldHandleExpectedSize() {
    BloomFilter<String> bloomFilter = new BloomFilter<>(100);
    bloomFilter.add("test");
    assertEquals(100, bloomFilter.getExpectedSize());
  }

  @Test
  public void shouldThrowExceptionForInvalidExpectedSize() {
    assertThrows(IllegalArgumentException.class, () -> new BloomFilter<>(-100));
  }

  @Test
  public void shouldHandleFitWithStream() {
    BloomFilter<String> bloomFilter = BloomFilter.fit(() -> Stream.of("test1", "test2", "test3"));
    assertTrue(bloomFilter.mightContain("test1"));
    assertTrue(bloomFilter.mightContain("test2"));
    assertTrue(bloomFilter.mightContain("test3"));
    assertFalse(bloomFilter.mightContain("test4"));
  }

  @Test
  public void shouldHandleMerge() {
    BloomFilter<String> bloomFilter1 = new BloomFilter<>(100);
    bloomFilter1.add("test1");

    BloomFilter<String> bloomFilter2 = new BloomFilter<>(100);
    bloomFilter2.add("test2");

    bloomFilter1.merge(bloomFilter2);

    assertTrue(bloomFilter1.mightContain("test1"));
    assertTrue(bloomFilter1.mightContain("test2"));
    assertFalse(bloomFilter1.mightContain("test3"));
  }

  @Test
  public void shouldThrowExceptionForMergeWithDifferentSizes() {
    BloomFilter<String> bloomFilter1 = new BloomFilter<>(100);
    BloomFilter<String> bloomFilter2 = new BloomFilter<>(200);
    assertThrows(IllegalArgumentException.class, () -> bloomFilter1.merge(bloomFilter2));
  }
}
