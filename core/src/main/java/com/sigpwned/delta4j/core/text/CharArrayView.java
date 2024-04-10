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

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

/**
 * A view of a slice of a string. A {@code CharArrayView} is a lightweight {@link CharSequence}
 * object that represents a logical slice of a character array. The slice is defined by a start
 * index (inclusive) and an end index (exclusive).
 *
 * @see UnmodifiableCharArrayView
 * @see MutableCharArrayView
 */
public abstract class CharArrayView<T extends CharArrayView<T>> implements CharSequence,
    Comparable<CharSequence> {

  /**
   * Creates a new {@link UnmodifiableCharArrayView} that represents the given slice of the given
   * character array. The slice is defined by the start index (inclusive) and the end index
   * (exclusive). No array copies are performed, so changes to the underlying array will be
   * reflected in the new slice.
   *
   * @param chars the character array
   * @param start the start index of the slice
   * @param end   the end index of the slice
   * @return the new slice
   */
  public static UnmodifiableCharArrayView unmodifiableOf(char[] chars, int start, int end) {
    return UnmodifiableCharArrayView.of(chars, start, end);
  }

  /**
   * Creates a new {@link MutableCharArrayView} that represents the given slice of the given
   * character array. The slice is defined by the start index (inclusive) and the end index
   * (exclusive). No array copies are performed, so changes to the underlying array will be
   * reflected in the new slice.
   *
   * @param chars the character array
   * @param start the start index of the slice
   * @param end   the end index of the slice
   * @return the new slice
   */
  public static MutableCharArrayView mutableOf(char[] chars, int start, int end) {
    return MutableCharArrayView.of(chars, start, end);
  }

  /**
   * The underlying character array.
   */
  protected final char[] chars;

  /**
   * The start index of the slice, inclusive.
   */
  protected int start;

  /**
   * The end index of the slice, exclusive.
   */
  protected int end;

  /**
   * The cached hash code of the slice.
   */
  private int hashCode;

  protected CharArrayView(char[] chars, int start, int end) {
    this.chars = requireNonNull(chars);
    if (start < 0 || start > chars.length) {
      throw new IllegalArgumentException("Invalid start index: " + start);
    }
    if (end < 0 || end > chars.length) {
      throw new IllegalArgumentException("Invalid end index: " + end);
    }
    if (start > end) {
      throw new IllegalArgumentException("Start index is greater than end index");
    }
    this.start = start;
    this.end = end;
  }

  /**
   * Returns the start index of the slice, inclusive. The domain of this method is the indexes of
   * the underlying character array.
   *
   * @return the start index of the slice
   */
  public final int start() {
    return start;
  }

  /**
   * Returns the end index of the slice, exclusive. The domain of this method is the indexes of the
   * underlying character array.
   *
   * @return the end index of the slice
   */
  public final int end() {
    return end;
  }

  /**
   * Returns the length of the slice, in characters. The domain of this method is the indexes of
   * this logical slice.
   *
   * @return the length of the slice
   */
  @Override
  public int length() {
    return lengthOfSlice();
  }

  /**
   * Returns the length of the slice, in characters. The domain of this method is the indexes of
   * this logical slice.
   *
   * @return the length of the slice
   */
  public int lengthOfSlice() {
    return end - start;
  }


  /**
   * Returns the length of the underlying character array. The domain of this method is the indexes
   * of the underlying character array.
   *
   * @return the length of the underlying character array
   */
  protected int lengthOfCharArray() {
    return chars.length;
  }

  /**
   * Returns the character at the given index in the slice. The index must be greater than or equal
   * to zero and less than the length of the slice.
   *
   * @param index the index of the character to return
   * @return the character at the given index
   * @throws IndexOutOfBoundsException if the index is out of bounds
   */
  @Override
  public char charAt(int index) {
    if (index < 0 || index >= lengthOfSlice()) {
      throw new IndexOutOfBoundsException("Index out of bounds: " + index);
    }
    return chars[start + index];
  }

  /**
   * Returns a new {@link CharArrayView} that represents the given slice of this slice. The new
   * slice is defined by the start index (inclusive) and the end index (exclusive). No array copies
   * are performed, so changes to the underlying array will be reflected in the new slice. The
   * domain of this method is the indexes of this logical slice.
   *
   * @param start the start index of the slice
   * @param end   the end index of the slice
   * @return the new slice
   */
  @Override
  public T subSequence(int start, int end) {
    if (start < 0) {
      throw new IllegalArgumentException("start must not be negative");
    }
    if (start > end) {
      throw new IllegalArgumentException("start must be less than or equal to end");
    }
    if (end > length()) {
      throw new IllegalArgumentException("end must be less than or equal to length");
    }
    return newCharArrayView(start + start, start + end);
  }

  protected abstract T newCharArrayView(int start, int end);

  /**
   * Returns the hash code of the slice. The hash code is computed lazily and cached. It uses the
   * same algorithm as {@link String#hashCode()}.
   *
   * @return the hash code of the slice
   */
  @Override
  public int hashCode() {
    if (hashCode != 0) {
      return hashCode;
    }
    for (int i = start; i < end; i++) {
      hashCode = 31 * hashCode + chars[i];
    }
    return hashCode;
  }

  /**
   * Compares this slice to another object. The slice is equal to another object if the other object
   * is a {@link CharSequence} with the same characters in the same order. This method is optimized
   * for comparing to other {@link CharArrayView} instances, but it can compare to any
   * {@link CharSequence} instance.
   *
   * @param other the object to compare to
   * @return {@code true} if the objects are equal, {@code false} otherwise
   */
  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    } else if (other == this) {
      return true;
    } else if (other instanceof CharArrayView<?> that) {
      if (this.chars == that.chars && this.start == that.start && this.end == that.end) {
        return true;
      }
      if (this.length() != that.length()) {
        return false;
      }
      return Arrays.equals(this.chars, this.start, this.end, that.chars, that.start, that.end);
    } else if (other instanceof CharSequence that) {
      if (that.length() != this.length()) {
        return false;
      }
      for (int i = 0; i < that.length(); i++) {
        if (that.charAt(i) != this.charAt(i)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Compares this slice to another {@link CharSequence}. The comparison is lexicographic, with
   * shorter slices coming before longer slices. This method is optimized for comparing to other
   * {@link CharArrayView} instances, but it can compare to any {@link CharSequence} instance.
   *
   * @param other the other {@link CharSequence} to compare to
   * @return a negative number if this slice comes before the other slice, a positive number if this
   * slice comes after the other slice, or zero if the slices are equal
   */
  @Override
  public int compareTo(CharSequence other) {
    if (this == other) {
      return 0;
    }
    if (other instanceof CharArrayView<?> that) {
      return Arrays.compare(this.chars, this.start, this.end, that.chars, that.start, that.end);
    }
    return CharSequence.compare(this, other);
  }

  /**
   * Returns a string containing the characters of the slice.
   *
   * @return a string containing the characters of the slice
   */
  @Override
  public String toString() {
    if (start == end) {
      return "";
    }
    if (start == 0 && end == lengthOfCharArray()) {
      return new String(chars);
    }
    return new String(chars, start, lengthOfSlice());
  }
}
