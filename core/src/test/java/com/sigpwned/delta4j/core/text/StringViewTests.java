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
package com.sigpwned.delta4j.core.text;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringViewTests {

  @Test
  public void shouldCreateImmutableStringView() {
    ImmutableStringView view = ImmutableStringView.of("abc", 0, 3);
    assertThat(view.length(), is(3));
    assertThat(view.charAt(0), is('a'));
    assertThat(view.charAt(1), is('b'));
    assertThat(view.charAt(2), is('c'));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionForInvalidImmutableStringView() {
    ImmutableStringView.of("abc", 0, 4);
  }

  @Test
  public void shouldCreateMutableStringView() {
    MutableStringView view = MutableStringView.of("abc", 0, 3);
    assertThat(view.length(), is(3));
    assertThat(view.charAt(0), is('a'));
    assertThat(view.charAt(1), is('b'));
    assertThat(view.charAt(2), is('c'));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionForInvalidMutableStringView() {
    MutableStringView.of("abc", 0, 4);
  }

  @Test
  public void shouldChangeMutableStringViewStart() {
    MutableStringView view = MutableStringView.of("abc", 0, 3);
    view.start(1);
    assertEquals(2, view.length());
    assertThat(view.charAt(0), is('b'));
    assertThat(view.charAt(1), is('c'));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionForInvalidStartMutableStringView() {
    MutableStringView view = MutableStringView.of("abc", 0, 3);
    view.start(4);
  }

  @Test
  public void shouldChangeMutableStringViewEnd() {
    MutableStringView view = MutableStringView.of("abc", 0, 3);
    view.end(2);
    assertEquals(2, view.length());
    assertThat(view.charAt(0), is('a'));
    assertThat(view.charAt(1), is('b'));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionForInvalidEndMutableStringView() {
    MutableStringView view = MutableStringView.of("abc", 0, 3);
    view.end(4);
  }

  @Test
  public void shouldChangeMutableStringViewRange() {
    MutableStringView view = MutableStringView.of("abc", 0, 3);
    view.range(1, 2);
    assertEquals(1, view.length());
    assertThat(view.charAt(0), is('b'));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionForInvalidRangeMutableStringView() {
    MutableStringView view = MutableStringView.of("abc", 0, 3);
    view.range(0, 4);
  }
}
