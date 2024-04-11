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

import org.junit.Test;

public class CharArrayViewTests {

  @Test
  public void shouldCreateUnmodifiableCharArrayView() {
    UnmodifiableCharArrayView view = UnmodifiableCharArrayView.of(new char[]{'a', 'b', 'c'}, 0, 3);
    assertThat(view.length(), is(3));
    assertThat(view.charAt(0), is('a'));
    assertThat(view.charAt(1), is('b'));
    assertThat(view.charAt(2), is('c'));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionForInvalidUnmodifiableCharArrayView() {
    UnmodifiableCharArrayView.of(new char[]{'a', 'b', 'c'}, 0, 4);
  }

  @Test
  public void shouldCreateMutableCharArrayView() {
    MutableCharArrayView view = MutableCharArrayView.of(new char[]{'a', 'b', 'c'}, 0, 3);
    assertThat(view.length(), is(3));
    assertThat(view.charAt(0), is('a'));
    assertThat(view.charAt(1), is('b'));
    assertThat(view.charAt(2), is('c'));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionForInvalidMutableCharArrayView() {
    MutableCharArrayView.of(new char[]{'a', 'b', 'c'}, 0, 4);
  }

  @Test
  public void shouldChangeMutableCharArrayView() {
    MutableCharArrayView view = MutableCharArrayView.of(new char[]{'a', 'b', 'c'}, 0, 3);
    view.charAt(0, 'd');
    assertThat(view.charAt(0), is('d'));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void shouldThrowExceptionForInvalidIndexMutableCharArrayView() {
    MutableCharArrayView view = MutableCharArrayView.of(new char[]{'a', 'b', 'c'}, 0, 3);
    view.charAt(3, 'd');
  }
}
