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

/**
 * A {@link CharSequence} implementation that represents a slice of a character array. Creating this
 * class is more efficient than creating a new {@link String} because it avoids copying the
 * underlying character array. This class is immutable, since it does not provide any mutator
 * methods, and the underlying string is immutable. This class is thread-safe.
 */
public final class ImmutableStringView extends StringView<ImmutableStringView> {

  public static final ImmutableStringView EMPTY = new ImmutableStringView("", 0, 0);

  public static ImmutableStringView empty() {
    return EMPTY;
  }

  public static ImmutableStringView of(String s) {
    return of(s, 0);
  }

  public static ImmutableStringView of(String s, int start) {
    return of(s, start, s.length());
  }

  public static ImmutableStringView of(String s, int start, int end) {
    return new ImmutableStringView(s, start, end);
  }

  public static ImmutableStringView fromMutableStringView(MutableStringView view) {
    return new ImmutableStringView(view.string, view.start, view.end);
  }

  /**
   * Creates a new {@link ImmutableStringView} that represents the given slice of the given string.
   *
   * @param s     the character array
   * @param start the start index of the slice
   * @param end   the end index of the slice
   */
  public ImmutableStringView(String s, int start, int end) {
    super(s, start, end);
  }

  @Override
  protected ImmutableStringView newStringView(int start, int end) {
    return new ImmutableStringView(string, start, end);
  }
}
