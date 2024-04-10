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
 * underlying character array. This class is unmodifiable, since it does not provide any mutator
 * methods, but is not immutable because it does not create a defensive copy of the underlying
 * character array. As a result, changes to the underlying character array will be reflected in this
 * slice. This class is thread-safe iff the user does not modify the underlying character array.
 */
public final class UnmodifiableCharArrayView extends CharArrayView<UnmodifiableCharArrayView> {

  public static final UnmodifiableCharArrayView EMPTY = new UnmodifiableCharArrayView(new char[0],
      0, 0);

  public static UnmodifiableCharArrayView empty() {
    return EMPTY;
  }

  public static UnmodifiableCharArrayView of(char[] chars, int start, int end) {
    return new UnmodifiableCharArrayView(chars, start, end);
  }

  public static UnmodifiableCharArrayView fromMutableCharArrayView(MutableCharArrayView view) {
    return new UnmodifiableCharArrayView(view.chars, view.start, view.end);
  }

  /**
   * Creates a new {@link UnmodifiableCharArrayView} that represents the given slice of the given
   * string. This method is deprecated because it creates a copy of the given string's underlying
   * character array, which is less efficient than the constructor that takes a character array.
   * Users should prefer to use the constructor that takes a character array whenever possible.
   *
   * @param s     the character array
   * @param start the start index of the slice
   * @param end   the end index of the slice
   * @see #UnmodifiableCharArrayView(char[], int, int)
   */
  @Deprecated
  public UnmodifiableCharArrayView(String s, int start, int end) {
    this(s.toCharArray(), start, end);
  }

  /**
   * Creates a new {@link UnmodifiableCharArrayView} that represents the given slice of the given
   * character array. The given character array is not copied, so changes to the array will be
   * reflected in this slice. The slice is defined by the start index (inclusive) and the end index
   * (exclusive).
   *
   * @param chars the character array
   * @param start the start index of the slice
   * @param end   the end index of the slice
   */
  public UnmodifiableCharArrayView(char[] chars, int start, int end) {
    super(chars, start, end);
  }

  @Override
  protected UnmodifiableCharArrayView newCharArrayView(int start, int end) {
    return new UnmodifiableCharArrayView(chars, start, end);
  }
}
