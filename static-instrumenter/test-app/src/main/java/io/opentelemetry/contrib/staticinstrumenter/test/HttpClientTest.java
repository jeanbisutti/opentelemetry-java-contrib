/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.staticinstrumenter.test;

import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

@SuppressWarnings("SystemOut")
public final class HttpClientTest {

  private HttpClientTest() {}

  public static void main(String[] args) throws Exception {
    String classPath = System.getProperty("java.class.path");
    System.out.println("classPath from SPring main= " + classPath);
    System.setProperty("otel.traces.exporter", "logging");
    makeCall();
  }

  private static void makeCall() throws IOException {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpGet request = new HttpGet("https://httpbin.org/get");
      try (CloseableHttpResponse response = httpClient.execute(request)) {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          Header traceparent = request.getFirstHeader("traceparent");
          if (traceparent != null) {
            System.out.println("SUCCESS");
            System.out.println("Traceparent value: " + traceparent.getValue());
            return;
          }
        }
      }
    }
    System.out.println("FAILURE");
    return;
  }
}
