/*
 * Copyright (c) 2019, 2020 Moataz Abdelnasser
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.mizosoft.methanol.convert;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.mizosoft.methanol.MediaType;
import com.github.mizosoft.methanol.TypeReference;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AbstractConverterTest {

  @Test
  void isCompatibleWith_single() {
    var converter = new ConverterImpl(MediaType.of("text", "*"));
    assertTrue(converter.isCompatibleWith(MediaType.of("text", "plain")));
    assertTrue(converter.isCompatibleWith(MediaType.of("text", "html")));
    assertFalse(converter.isCompatibleWith(MediaType.of("application", "octet-stream")));
    assertTrue(Set.of(MediaType.of("text", "*")).containsAll(converter.compatibleMediaTypes()));
  }

  @Test
  void isCompatibleWith_multiple() {
    var converter = new ConverterImpl(
        MediaType.of("text", "plain"), MediaType.of("application", "json"));
    assertTrue(converter.isCompatibleWith(MediaType.of("text", "plain")));
    assertTrue(converter.isCompatibleWith(MediaType.of("application", "json")));
    assertTrue(converter.isCompatibleWith(MediaType.of("application", "*")));
    assertFalse(converter.isCompatibleWith(MediaType.of("application", "octet_stream")));
    var types = Set.of(MediaType.of("text", "plain"), MediaType.of("application", "json"));
    assertTrue(types.containsAll(converter.compatibleMediaTypes()));
  }

  @Test
  void requireSupport() {
    var converter = new AbstractConverter() {
      @Override
      public boolean supportsType(TypeReference<?> type) {
        return List.class.isAssignableFrom(type.rawType());
      }
    };
    assertThrows(UnsupportedOperationException.class,
        () -> converter.requireSupport(TypeReference.from(Set.class)));
  }

  @Test
  void requireCompatibleOrNull() {
    var converter = new ConverterImpl(MediaType.of("text", "plain"));
    assertThrows(UnsupportedOperationException.class,
        () -> converter.requireCompatibleOrNull(MediaType.of("application", "json")));
  }

  private static final class ConverterImpl extends AbstractConverter {

    ConverterImpl(MediaType... compatibleMediaTypes) {
      super(compatibleMediaTypes);
    }

    @Override
    public boolean supportsType(TypeReference<?> type) {
      return false;
    }
  }
}