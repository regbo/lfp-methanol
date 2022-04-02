/*
 * Copyright (c) 2022 Moataz Abdelnasser
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

package com.github.mizosoft.methanol.testutils;

import static java.net.HttpURLConnection.HTTP_OK;

import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse.ResponseInfo;

public final class FakeResponseInfo implements ResponseInfo {
  private final int statusCode;
  private final HttpHeaders headers;
  private final Version version;

  public FakeResponseInfo() {
    this(HTTP_OK, TestUtils.headers(/* empty */ ), Version.HTTP_1_1);
  }

  public FakeResponseInfo(int statusCode, HttpHeaders headers, Version version) {
    this.statusCode = statusCode;
    this.headers = headers;
    this.version = version;
  }

  @Override
  public int statusCode() {
    return statusCode;
  }

  @Override
  public HttpHeaders headers() {
    return headers;
  }

  @Override
  public Version version() {
    return version;
  }
}
