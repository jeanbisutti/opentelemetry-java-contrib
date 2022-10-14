/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.staticinstrumenter.plugin.maven;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;

class OpenTelemetryInstrumenterMojoTest extends AbstractTempDirTest {

  @Test
  void shouldInstrumentSampleApplication() throws Exception {
    // given
    OpenTelemetryInstrumenterMojo mojo = new OpenTelemetryInstrumenterMojo();
    String pathOfSourceFile = "C:\\agent\\instrumented\\ai\\source\\spring-petclinic-2.7.0-SNAPSHOT.jar";
    Path testApp = Path.of(pathOfSourceFile);
    mojo.vendorAgentPath = "C:\\agent\\instrumented\\ai\\agent";
    mojo.vendorAgentMainClass = "com.microsoft.applicationinsights.agent.Agent";

    // when
    try {
     mojo.executeInternal("C:\\agent\\instrumented\\ai\\instrumented-artifact", "-instrumented", Collections.singletonList(testApp));
    } catch(Exception e) {
      e.printStackTrace();
    }

    // copy  ended with .zip

   /*
    String targetDir = "C:\\agent\\instrumented\\ai\\instrumented-artifact\\";
    String pathOfInstrumentedJar = targetDir + "\\spring-petclinic-2.7.0-SNAPSHOT-instrumented.jar";
    File instrumentedJarFile = new File(pathOfInstrumentedJar);
    String zipPath = targetDir + "spring-petclinic-2.7.0-SNAPSHOT-instrumented.zip";
    File zipFile = new File(zipPath);
    instrumentedJarFile.renameTo(zipFile);
*/

    // Unzip

    // then
    //Path instrumentedApp = tempDir.toPath().resolve("test-http-app-instrumented.jar");
    //assertThat(Files.exists(instrumentedApp)).isTrue();
    //verifyApplicationByExampleRun(instrumentedApp);

  }

  /**
   * Test application does an http call using Apache HTTP client. If a response contains
   * "Traceparent" header (result of autoinstrumentation), application writes "SUCCESS" to system
   * out.
   */
  private static void verifyApplicationByExampleRun(Path instrumentedApp) throws Exception {
    ProcessBuilder pb =
        new ProcessBuilder("java", "-jar", instrumentedApp.toString()).redirectErrorStream(true);
    Process process = pb.start();
    process.waitFor();
    String output = new String(process.getInputStream().readAllBytes(), Charset.defaultCharset());
    assertThat(output).contains("SUCCESS");

    InputStream errorStream = process.getErrorStream();
    String errorOutput =
        new String(process.getInputStream().readAllBytes(), Charset.defaultCharset());
    System.out.println("errorOutput = " + errorOutput);
    System.out.println("End of error output");
  }
}
