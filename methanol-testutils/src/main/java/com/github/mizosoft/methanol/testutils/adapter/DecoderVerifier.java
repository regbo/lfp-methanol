/*
 * Copyright (c) 2021 Moataz Abdelnasser
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

package com.github.mizosoft.methanol.testutils.adapter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.github.mizosoft.methanol.BodyAdapter.Decoder;
import com.github.mizosoft.methanol.MediaType;
import com.github.mizosoft.methanol.TypeRef;
import java.net.http.HttpResponse.BodySubscriber;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Supplier;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.ThrowableAssertAlternative;
import org.checkerframework.checker.nullness.qual.Nullable;

/** A small DSL for testing {@link Decoder} implementations. */
public final class DecoderVerifier extends BodyAdapterVerifier<Decoder, DecoderVerifier> {
  DecoderVerifier(Decoder decoder) {
    super(decoder);
  }

  @Override
  DecoderVerifier self() {
    return this;
  }

  public <T> BodyConversionStep<T> converting(TypeRef<T> type) {
    return new BodyConversionStep<>(adapter, type);
  }

  public <T> BodyConversionStep<T> converting(Class<T> type) {
    return converting(TypeRef.from(type));
  }

  public static final class BodyConversionStep<T> {
    private final Decoder decoder;
    private final TypeRef<T> type;
    private final @Nullable MediaType mediaType;

    BodyConversionStep(Decoder decoder, TypeRef<T> type) {
      this(decoder, type, null);
    }

    BodyConversionStep(Decoder decoder, TypeRef<T> type, @Nullable MediaType mediaType) {
      this.decoder = decoder;
      this.type = type;
      this.mediaType = mediaType;
    }

    public BodyConversionStep<T> withMediaType(String mediaType) {
      return withMediaType(MediaType.parse(mediaType));
    }

    public BodyConversionStep<T> withMediaType(MediaType mediaType) {
      return new BodyConversionStep<>(decoder, type, mediaType);
    }

    public ThrowableAssertAlternative<UnsupportedOperationException> isNotSupported() {
      assertThatExceptionOfType(UnsupportedOperationException.class)
          .isThrownBy(() -> decoder.toObject(type, mediaType));
      return assertThatExceptionOfType(UnsupportedOperationException.class)
          .isThrownBy(() -> decoder.toDeferredObject(type, mediaType));
    }

    public BodySubscriberAssert<T> withBody(String body) {
      return withBody(body, UTF_8);
    }

    public BodySubscriberAssert<T> withBody(String body, Charset charset) {
      return withBody(charset.encode(body));
    }

    public BodySubscriberAssert<T> withBody(ByteBuffer body) {
      var subscriber = decoder.toObject(type, mediaType);
      publisherBody(subscriber, body);
      return new BodySubscriberAssert<>(subscriber);
    }

    public BodySubscriberAssert<T> withFailure(Throwable error) {
      var subscriber = decoder.toObject(type, mediaType);
      publisherError(subscriber, error);
      return new BodySubscriberAssert<>(subscriber);
    }

    public SupplierAssert<T> withDeferredBody(String body) {
      return withDeferredBody(body, UTF_8);
    }

    public SupplierAssert<T> withDeferredBody(String body, Charset charset) {
      return withDeferredBody(charset.encode(body));
    }

    public SupplierAssert<T> withDeferredBody(ByteBuffer body) {
      var subscriber = decoder.toDeferredObject(type, mediaType);
      publisherBody(subscriber, body);
      return supplierAssert(subscriber);
    }

    public SupplierAssert<T> withDeferredFailure(Throwable error) {
      var subscriber = decoder.toDeferredObject(type, mediaType);
      publisherError(subscriber, error);
      return supplierAssert(subscriber);
    }

    private SupplierAssert<T> supplierAssert(BodySubscriber<Supplier<T>> subscriber) {
      var bodyFuture = subscriber.getBody();
      assertThat(bodyFuture).isCompleted().isNotCancelled();
      return new SupplierAssert<>(bodyFuture.toCompletableFuture().join());
    }

    private static void publisherBody(BodySubscriber<?> subscriber, ByteBuffer buffer) {
      try (var publisher = new SubmissionPublisher<List<ByteBuffer>>()) {
        publisher.subscribe(subscriber);
        publisher.submit(List.of(buffer));
      }
    }

    private static void publisherError(BodySubscriber<?> subscriber, Throwable error) {
      try (var publisher = new SubmissionPublisher<List<ByteBuffer>>()) {
        publisher.subscribe(subscriber);
        publisher.closeExceptionally(error);
      }
    }
  }

  public static final class BodySubscriberAssert<T> {
    private final BodySubscriber<T> subscriber;

    BodySubscriberAssert(BodySubscriber<T> subscriber) {
      this.subscriber = subscriber;
    }

    public ObjectAssert<T> completedBody() {
      return assertThat(subscriber.getBody()).isCompleted().succeedsWithin(Duration.ZERO);
    }

    public ObjectAssert<T> succeedsWith(T obj) {
      return assertThat(subscriber.getBody()).succeedsWithin(Duration.ofSeconds(20)).isEqualTo(obj);
    }

    public ThrowableAssertAlternative<?> failsWith(Class<? extends Throwable> type) {
      return assertThat(subscriber.getBody())
          .failsWithin(Duration.ofSeconds(20))
          .withThrowableOfType(ExecutionException.class)
          .havingCause()
          .isInstanceOf(type);
    }
  }

  public static final class SupplierAssert<T> {
    private final Supplier<T> supplier;

    SupplierAssert(Supplier<T> supplier) {
      this.supplier = supplier;
    }

    public AbstractObjectAssert<?, T> succeedsWith(T obj) {
      return assertThat(supplier.get()).isEqualTo(obj);
    }

    public ThrowableAssertAlternative<? extends Throwable> failsWith(
        Class<? extends Throwable> type) {
      return assertThatExceptionOfType(type).isThrownBy(supplier::get);
    }
  }
}
