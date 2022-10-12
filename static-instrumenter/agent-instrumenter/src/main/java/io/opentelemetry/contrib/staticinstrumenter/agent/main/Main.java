/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.staticinstrumenter.agent.main;

import io.opentelemetry.contrib.staticinstrumenter.util.SystemLogger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class Main {

  private static final SystemLogger logger = SystemLogger.getLogger(Main.class);

  private static final Main INSTANCE = new Main(ClassArchive::new);

  private final ClassArchive.Factory classArchiveFactory;

  // key is slashy name, not dotty
  private final Map<String, byte[]> instrumentedClasses = new ConcurrentHashMap<>();

  public static void main(String[] args) throws Exception {

    if (args.length != 1) {
      printUsage();
      return;
    }

    File outDir = new File(args[0]);
    if (!outDir.exists()) {
      outDir.mkdir();
    }

    String classPath = System.getProperty("java.class.path");
    logger.debug("Classpath (jars list): {}", classPath);
    System.out.println("classPath = " + classPath);

    String[] jarsList = classPath.split(File.pathSeparator);

    getInstance().saveTransformedJarsTo(jarsList, outDir);
  }

  @SuppressWarnings("SystemOut")
  private static void printUsage() {
    System.out.println(
        "OpenTelemetry Java Static Instrumenter\n"
            + "Usage:\njava "
            + Main.class.getCanonicalName()
            + " <output directory> (where instrumented archives will be stored)");
  }

  public static Main getInstance() {
    return INSTANCE;
  }

  public static ClassFileTransformer getPreTransformer() {
    return new PreTransformer();
  }

  public static ClassFileTransformer getPostTransformer() {
    return new PostTransformer();
  }

  // for testing purposes
  Main(ClassArchive.Factory classArchiveFactory) {
    this.classArchiveFactory = classArchiveFactory;
  }

  // FIXME: java 9 / jmod support, proper handling of directories
  // FIXME: jmod in particular introduces weirdness with adding helpers to the dependencies

  /**
   * Copies all class archives (JARs, WARs) to outDir. Classes that were instrumented and stored in
   * instrumentedClasses will get replaced with the new version. All classes added to
   * additionalClasses will be added to the new archive.
   *
   * @param outDir directory where jars will be written
   * @throws IOException in case of file operation problem
   */
  public void saveTransformedJarsTo(String[] jarsList, File outDir) throws IOException {

    for (String pathItem : jarsList) {
      String message = "Classpath item processed: " + pathItem;
      System.out.println("message = " + message);
      logger.info(message);
      if (isArchive(pathItem)) {
        saveArchiveTo(new File(pathItem), outDir);
      }
    }
  }

  private static boolean isArchive(String pathItem) {
    return (pathItem.endsWith(".jar") || pathItem.endsWith(".war"));
  }

  // FIXME: don't "instrument" our agent jar
  // FIXME: detect and warn on signed jars (and drop the signing bits)
  // FIXME: multiple jars with same name
  private void saveArchiveTo(File inFile, File outDir) throws IOException {

    String subFolderPath = extractSubfolderPath(inFile.toString());

    File outDirUntilSubDir = new File(outDir.toString() + subFolderPath);
    if(!outDirUntilSubDir.exists()) {
      outDirUntilSubDir.mkdirs();
    }
    try (JarFile inJar = new JarFile(inFile);
        JarOutputStream outJar = jarOutputStreamFor(outDirUntilSubDir, inFile.getName())) {
      ClassArchive inClassArchive = classArchiveFactory.createFor(inJar, instrumentedClasses);
      inClassArchive.copyAllClassesTo(outJar);
      injectAdditionalClassesTo(outJar);
    }
  }

  private static String extractSubfolderPath(String inFile) {
    String result = "";
    String[] elements = inFile.split("PREPARATION_FOLDER")[1].split("\\\\");
    System.out.println("elements = " + Arrays.asList(elements));
    for(int i = 1; i < (elements.length -1); i++) {
      result = result + elements[i] + File.separator;
    }
    if(!result.isEmpty()) {
      result = File.separator + result;
    }
    return result;
  }


  private static JarOutputStream jarOutputStreamFor(File outDir, String fileName)
      throws IOException {
    File outFile = new File(outDir, fileName);
    String outFileAsString = outFile.toString();
    System.out.println("outFileAsString = " + outFileAsString);


    return new JarOutputStream(new FileOutputStream(outFile));
  }

  // FIXME: only relevant additional classes should be injected
  private static void injectAdditionalClassesTo(JarOutputStream outJar) throws IOException {
    for (Map.Entry<String, byte[]> entry : AdditionalClasses.get().entrySet()) {
      String className = entry.getKey();
      byte[] classData = entry.getValue();

      ZipEntry newEntry = new ZipEntry(className);
      outJar.putNextEntry(newEntry);
      if (classData != null) {
        newEntry.setSize(classData.length);
        outJar.write(classData);
      }
      outJar.closeEntry();

      logger.debug("Additional class added: {}", className);
    }
  }

  public Map<String, byte[]> getInstrumentedClasses() {
    return instrumentedClasses;
  }
}
