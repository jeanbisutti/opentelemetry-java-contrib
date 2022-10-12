/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.staticinstrumenter.plugin.maven;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class ZipEntryCreator {

  private ZipEntryCreator() {}

  static void moveEntryUpdating(
      FileSystem targetFs, String targetPath, JarEntry sourceEntry, JarFile sourceJar)
      throws IOException {

    Path entry = targetFs.getPath("/", targetPath);
    Path parentPath = entry.getParent();
    //System.out.println("parentPath = " + parentPath);
    // Not necessary to move  /BOOT-INF/classes/io/opentelemetry/javaagent/instrumentation/ ????
    Files.createDirectories(parentPath);

    if(!"/BOOT-INF/classes/META-INF".equals(parentPath.toString())) { // do not copy MANIFEST.MF from open telemetry static agent
      try (InputStream sourceInput = sourceJar.getInputStream(sourceEntry)) {
        Files.copy(sourceInput, entry);
      }
    }

  }

  static void moveEntry(
      ZipOutputStream targetOut, String targetPath, JarEntry sourceEntry, JarFile sourceJar)
      throws IOException {

    ZipEntry entry = new ZipEntry(targetPath);
    try (InputStream sourceInput = sourceJar.getInputStream(sourceEntry)) {

      entry.setSize(sourceEntry.getSize());
      entry.setCompressedSize(sourceEntry.getCompressedSize());
      entry.setMethod(sourceEntry.getMethod());
      entry.setCrc(sourceEntry.getCrc());

      targetOut.putNextEntry(entry);
      sourceInput.transferTo(targetOut);
      targetOut.closeEntry();
    }
  }

  static void createZipEntryFromFile(ZipOutputStream targetOut, Path sourceFile, String entryPath)
      throws IOException {

    ZipEntry entry = new ZipEntry(entryPath);


    entry.setMethod(Deflater.NO_COMPRESSION);
    /* FIX FOR
    $ java -jar target/spring-petclinic-2.7.0-SNAPSHOT-instrumented.jar
Exception in thread "main" java.lang.IllegalStateException: Failed to get nested archive for entry BOOT-INF/lib/logback-classic-1.2.11.jar
        at org.springframework.boot.loader.archive.JarFileArchive.getNestedArchive(JarFileArchive.java:120)
        at org.springframework.boot.loader.archive.JarFileArchive$NestedArchiveIterator.adapt(JarFileArchive.java:274)
        at org.springframework.boot.loader.archive.JarFileArchive$NestedArchiveIterator.adapt(JarFileArchive.java:265)
        at org.springframework.boot.loader.archive.JarFileArchive$AbstractIterator.next(JarFileArchive.java:226)
        at org.springframework.boot.loader.ExecutableArchiveLauncher.createClassLoader(ExecutableArchiveLauncher.java:104)
        at org.springframework.boot.loader.Launcher.launch(Launcher.java:55)
        at org.springframework.boot.loader.JarLauncher.main(JarLauncher.java:65)
Caused by: java.io.IOException: Unable to open nested jar file 'BOOT-INF/lib/logback-classic-1.2.11.jar'
        at org.springframework.boot.loader.jar.JarFile.getNestedJarFile(JarFile.java:312)
        at org.springframework.boot.loader.jar.JarFile.getNestedJarFile(JarFile.java:298)
        at org.springframework.boot.loader.archive.JarFileArchive.getNestedArchive(JarFileArchive.java:116)
        ... 6 more
Caused by: java.lang.IllegalStateException: Unable to open nested entry 'BOOT-INF/lib/logback-classic-1.2.11.jar'. It has been compressed and nested jar files must be stored without compression. Please check the mechanism used to create your executable jar file
        at org.springframework.boot.loader.jar.JarFile.createJarFileFromFileEntry(JarFile.java:338)
        at org.springframework.boot.loader.jar.JarFile.createJarFileFromEntry(JarFile.java:320)
        at org.springframework.boot.loader.jar.JarFile.getNestedJarFile(JarFile.java:309)
        ... 8 more
     */

    byte[] bytes = Files.readAllBytes(sourceFile);
    entry.setSize(bytes.length);
    CRC32 crc = new CRC32();
    crc.update(bytes);
    entry.setCrc(crc.getValue());

    targetOut.putNextEntry(entry);
    targetOut.write(bytes);
    targetOut.closeEntry();
  }

}
