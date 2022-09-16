/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.startupprofiler;

import com.microsoft.jfr.FlightRecorderConnection;
import com.microsoft.jfr.JfrStreamingException;
import com.microsoft.jfr.Recording;
import com.microsoft.jfr.RecordingConfiguration;
import com.microsoft.jfr.RecordingOptions;
import com.microsoft.jfr.dcmd.FlightRecorderDiagnosticCommandConnection;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import javax.annotation.Nullable;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;

public final class StartupProfilerAgent {

  @SuppressWarnings("SystemOut")
  public static void premain(String agentArgs, Instrumentation inst)
      throws InstanceNotFoundException, IOException, JfrStreamingException {

    System.out.println("Start-up profiling begins");

    FlightRecorderConnection flightRecorderConnection = buildFlightRecorderConnection();

    if (flightRecorderConnection != null) {
      RecordingOptions recordingOptions =
          new RecordingOptions.Builder()
              .disk("true")
              .duration("30 s")
              .dumpOnExit("true")
              .destination("C:\\agent\\startup-profiling.jfr")
              .build();
      RecordingConfiguration recordingConfiguration = RecordingConfiguration.PROFILE_CONFIGURATION;

      Recording recording =
          flightRecorderConnection.newRecording(recordingOptions, recordingConfiguration);
      recording.start();
    }
  }

  @Nullable
  private static FlightRecorderConnection buildFlightRecorderConnection()
      throws IOException, InstanceNotFoundException, JfrStreamingException {

    MBeanServerConnection mbeanServer = ManagementFactory.getPlatformMBeanServer();
    try {
      try {
        return FlightRecorderConnection.connect(mbeanServer);
      } catch (IOException e) {

      }
    } catch (JfrStreamingException | InstanceNotFoundException jfrStreamingException) {
      return FlightRecorderDiagnosticCommandConnection.connect(mbeanServer);
    }

    return null;
  }

  private StartupProfilerAgent() {}
}
