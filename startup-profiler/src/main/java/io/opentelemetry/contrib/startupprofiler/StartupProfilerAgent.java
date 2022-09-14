/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.startupprofiler;

import java.lang.instrument.Instrumentation;

public final class StartupProfilerAgent {

  @SuppressWarnings("SystemOut")
  public static void premain(String agentArgs, Instrumentation inst) {

    System.out.println("Start-up profiling begins");

  }

  private StartupProfilerAgent() {
  }
}
