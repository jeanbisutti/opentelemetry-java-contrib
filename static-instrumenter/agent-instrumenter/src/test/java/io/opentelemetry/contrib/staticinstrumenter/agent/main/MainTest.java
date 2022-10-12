/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.staticinstrumenter.agent.main;

import static io.opentelemetry.contrib.staticinstrumenter.agent.main.JarTestUtil.getResourcePath;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MainTest {

  @Test
  void shouldInjectAdditionalClasses(@TempDir File destination) throws IOException {

    // given
    ClassArchive.Factory factory = mock(ClassArchive.Factory.class);
    ClassArchive mockArchive = mock(ClassArchive.class);
    when(factory.createFor(any(), anyMap())).thenReturn(mockArchive);
    Main underTest = new Main(factory);
    AdditionalClasses.put("additionalOne.class", new byte[0]);
    AdditionalClasses.put("additionalTwo.class", new byte[0]);

    // when
    String pathOfInstrumentationWorkingFolder = "";
    underTest.saveTransformedJarsTo(new String[] {getResourcePath("test.jar")}, destination);

    // then
    JarTestUtil.assertJar(
        destination, "test.jar", new String[] {"additionalOne.class", "additionalTwo.class"}, null);
  }


  @Test
  void for_out_dir() {
    String inFile = "C:\\Users\\JEANBI~1\\AppData\\Local\\Temp\\PREPARATION_FOLDER10029891967703798809\\BOOT-INF\\lib\\logback-classic-1.2.11.jar";
    String result = extractSubfolderPath(inFile);
    System.out.println("result = " + result);

    String inFile2 = "C:\\Users\\JEANBI~1\\AppData\\Local\\Temp\\PREPARATION_FOLDER10029891967703798809\\logback-classic-1.2.11.jar";
    String result2 = extractSubfolderPath(inFile2);
    System.out.println("result2 = " + result2);

  }

  private static String extractSubfolderPath(String inFile) {
    String result = "";
    String[] elements = inFile.split("PREPARATION_FOLDER")[1].split("\\\\");
    System.out.println("elements = " + Arrays.asList(elements));
    for(int i = 1; i < (elements.length -1); i++) {
      result = result + elements[i] + File.separator;
    }
    return result;
  }

}
