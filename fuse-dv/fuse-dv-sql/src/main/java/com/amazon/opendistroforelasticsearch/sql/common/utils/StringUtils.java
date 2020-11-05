/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.common.utils;

import com.google.common.base.Strings;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Locale;

public class StringUtils {
  /**
   * Unquote any string with mark specified.
   * @param text string
   * @param mark quotation mark
   * @return An unquoted string whose outer pair of (single/double/back-tick) quotes have been
   *     removed
   */
  public static String unquote(String text, String mark) {
    if (isQuoted(text, mark)) {
      return text.substring(mark.length(), text.length() - mark.length());
    }
    return text;
  }

  /**
   * Unquote Identifier which has " or ' or ` as mark.
   * @param text string
   * @return An unquoted string whose outer pair of (single/double/back-tick) quotes have been
   *     removed
   */
  public static String unquoteText(String text) {
    if (isQuoted(text, "\"") || isQuoted(text, "'") || isQuoted(text, "`")) {
      return text.substring(1, text.length() - 1);
    } else {
      return text;
    }
  }

  /**
   * Unquote Identifier which has " or ` as mark.
   * @param identifier identifier that possibly enclosed by double quotes or back ticks
   * @return An unquoted string whose outer pair of (double/back-tick) quotes have been
   *     removed
   */
  public static String unquoteIdentifier(String identifier) {
    if (isQuoted(identifier, "\"") || isQuoted(identifier, "`")) {
      return identifier.substring(1, identifier.length() - 1);
    } else {
      return identifier;
    }
  }

  /**
   * Returns a formatted string using the specified format string and
   * arguments, as well as the {@link Locale#ROOT} locale.
   *
   * @param format format string
   * @param args   arguments referenced by the format specifiers in the format string
   * @return A formatted string
   * @throws IllegalFormatException If a format string contains an illegal syntax, a format
   *                                specifier that is incompatible with the given arguments,
   *                                insufficient arguments given the format string, or other
   *                                illegal conditions.
   * @see java.lang.String#format(Locale, String, Object...)
   */
  public static String format(final String format, Object... args) {
    return String.format(Locale.ROOT, format, args);
  }

  private static boolean isQuoted(String text, String mark) {
    return !Strings.isNullOrEmpty(text) && text.startsWith(mark) && text.endsWith(mark);
  }


  public static String newString(byte[] val, int index, int len) {
    return new String(Arrays.copyOfRange(val, index, index + len));
  }

  public static String strip(byte[] value) {
    int left = indexOfNonWhitespace(value);
    if (left == value.length) {
      return "";
    }
    int right = lastIndexOfNonWhitespace(value);
    return ((left > 0) || (right < value.length)) ? newString(value, left, right - left) : null;
  }

  public static String stripLeading(byte[] value) {
    int left = indexOfNonWhitespace(value);
    if (left == value.length) {
      return "";
    }
    return (left != 0) ? newString(value, left, value.length - left) : null;
  }

  public static String stripTrailing(byte[] value) {
    int right = lastIndexOfNonWhitespace(value);
    if (right == 0) {
      return "";
    }
    return (right != value.length) ? newString(value, 0, right) : null;
  }

  public static int indexOfNonWhitespace(byte[] value) {
    int length = value.length;
    int left = 0;
    while (left < length) {
      char ch = (char)(value[left] & 0xff);
      if (ch != ' ' && ch != '\t' && !Character.isWhitespace(ch)) {
        break;
      }
      left++;
    }
    return left;
  }

  public static int lastIndexOfNonWhitespace(byte[] value) {
    int length = value.length;
    int right = length;
    while (0 < right) {
      char ch = (char)(value[right - 1] & 0xff);
      if (ch != ' ' && ch != '\t' && !Character.isWhitespace(ch)) {
        break;
      }
      right--;
    }
    return right;
  }

}
