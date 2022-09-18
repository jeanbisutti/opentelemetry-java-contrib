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

  private static final String PROFILING_DURATION_IN_SECONDS =
      "-Dotel.startupprofiler.duration-in-seconds";

  private static final String PROFILING_FILE = "-Dotel.startupprofiler.file";

  @SuppressWarnings("SystemOut")
  public static void premain(String agentArgs, Instrumentation inst)
      throws InstanceNotFoundException, IOException, JfrStreamingException {

    String durationInSeconds = System.getProperty(PROFILING_DURATION_IN_SECONDS);

    String profilingFile = System.getProperty(PROFILING_FILE);
    if (profilingFile == null) {
      new IllegalStateException("");
    }

    System.out.println("Start-up profiling begins");

    FlightRecorderConnection flightRecorderConnection = buildFlightRecorderConnection();

    if (flightRecorderConnection != null) {
      RecordingOptions recordingOptions =
          buildFrecordingOptionsFrom(durationInSeconds, profilingFile);
      RecordingConfiguration recordingConfiguration = RecordingConfiguration.PROFILE_CONFIGURATION;
      Recording recording =
          flightRecorderConnection.newRecording(recordingOptions, recordingConfiguration);
      recording.start();
    }
  }

  private static RecordingOptions buildFrecordingOptionsFrom(
      String durationInSeconds, String profilingFile) {
    RecordingOptions.Builder recordingOptionsBuilder =
        new RecordingOptions.Builder().disk("true").dumpOnExit("true").destination(profilingFile);
    if (durationInSeconds != null) {
      recordingOptionsBuilder = recordingOptionsBuilder.duration(durationInSeconds + " s");
    }
    return recordingOptionsBuilder.build();
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
