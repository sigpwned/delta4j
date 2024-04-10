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
 * underlying character array. This class is explicitly mutable, since it provides several mutator
 * methods. This class is not thread-safe.
 */
public final class MutableStringView extends StringView<MutableStringView> {

  public static MutableStringView of(String s, int start, int end) {
    return new MutableStringView(s, start, end);
  }

  /**
   * Creates a new {@link MutableStringView} that represents the given slice of the given string.
   * This method is deprecated because it creates a copy of the given string's underlying character
   * array, which is less efficient than the constructor that takes a character array. Users should
   * prefer to use the constructor that takes a character array whenever possible.
   *
   * @param s     the character array
   * @param start the start index of the slice
   * @param end   the end index of the slice
   */
  public MutableStringView(String s, int start, int end) {
    super(s, start, end);
  }

  @Override
  protected MutableStringView newStringView(int start, int end) {
    return new MutableStringView(string, start, end);
  }

  /**
   * Sets the start position of the slice. The new start position must be greater than or equal to
   * zero and less than or equal to the end position. The domain of this method is the indexes of
   * the underlying character array.
   *
   * @param start the new start position
   * @throws IllegalArgumentException if the new start position is out of bounds
   */
  public void start(int start) {
    if (start < 0 || start > string.length()) {
      throw new IllegalArgumentException("Invalid start index: " + start);
    }
    if (start > end) {
      throw new IllegalArgumentException("New start index is greater than current end index");
    }
    this.start = start;
  }

  /**
   * Sets the end position of the slice. The new end position must be greater than or equal to the
   * start position and less than or equal to the length of the underlying character array. The
   * domain of this method is the indexes of the underlying character array.
   *
   * @param end the new end position
   * @throws IllegalArgumentException if the new end position is out of bounds
   */
  public void end(int end) {
    if (end < 0 || end > string.length()) {
      throw new IllegalArgumentException("Invalid end index: " + end);
    }
    if (end < start) {
      throw new IllegalArgumentException("New end index is less than current start index");
    }
    this.end = end;
  }

  /**
   * Sets the start and end positions of the slice. The new start position must be greater than or
   * equal to zero and less than or equal to the new end position. The domain of this method is the
   * indexes of the underlying character array.
   *
   * @param start the new start position
   * @param end   the new end position
   * @throws IllegalArgumentException if the new start or end positions are out of bounds
   */
  public void range(int start, int end) {
    if (start < 0 || start > string.length()) {
      throw new IllegalArgumentException("Invalid start index: " + start);
    }
    if (end < 0 || end > string.length()) {
      throw new IllegalArgumentException("Invalid end index: " + end);
    }
    if (start > end) {
      throw new IllegalArgumentException("Start index is greater than end index");
    }
    this.start = start;
    this.end = end;
  }

  @Override
  public int lengthOfString() {
    return string.length();
  }
}
