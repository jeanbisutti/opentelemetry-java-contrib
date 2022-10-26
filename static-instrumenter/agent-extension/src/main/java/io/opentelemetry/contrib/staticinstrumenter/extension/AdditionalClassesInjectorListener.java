/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.staticinstrumenter.extension;

import io.opentelemetry.contrib.staticinstrumenter.agent.main.AdditionalClasses;
import io.opentelemetry.javaagent.tooling.HelperInjectorListener;
import java.util.Map;

/**
 * A listener to be registered in {@link io.opentelemetry.javaagent.tooling.HelperInjector}. It
 * saves all additional classes created by the agent to the AdditionalClasses class.
 *
 *     JDK VIRTUAL FIELDS
 *     Additional class: io/opentelemetry/javaagent/bootstrap/field/VirtualFieldAccessor$ java$util$concurrent$Callable $io$opentelemetry$javaagent$bootstrap$executors$PropagatedContext.class
 *     Additional class: io/opentelemetry/javaagent/bootstrap/field/VirtualFieldAccessor$ java$lang$Runnable $io$opentelemetry$javaagent$bootstrap$executors$PropagatedContext.class
 *     Additional class: io/opentelemetry/javaagent/bootstrap/field/VirtualFieldAccessor$ java$util$concurrent$Future $io$opentelemetry$javaagent$bootstrap$executors$PropagatedContext.class
 *     Additional class: io/opentelemetry/javaagent/bootstrap/field/VirtualFieldAccessor$ java$util$concurrent$ForkJoinTask $io$opentelemetry$javaagent$bootstrap$executors$PropagatedContext.class
 *     Additional class: io/opentelemetry/javaagent/bootstrap/field/VirtualFieldImpl$ java$util$concurrent$ForkJoinTask $io$opentelemetry$javaagent$bootstrap$executors$PropagatedContext.class
 *     Additional class: io/opentelemetry/javaagent/bootstrap/field/VirtualFieldImpl$ java$util$concurrent$Callable $io$opentelemetry$javaagent$bootstrap$executors$PropagatedContext.class
 *     Additional class: io/opentelemetry/javaagent/bootstrap/field/VirtualFieldImpl$ java$util$concurrent$Future $io$opentelemetry$javaagent$bootstrap$executors$PropagatedContext.class
 *     Additional class: io/opentelemetry/javaagent/bootstrap/field/VirtualFieldImpl$ java$lang$Runnable $io$opentelemetry $javaagent$bootstrap$executors$PropagatedContext.class
 *
 *     JDK INSTRUMENTATION CLASSES
 *     Additional class: io/opentelemetry/javaagent/instrumentation/internal/lambda/LambdaTransformer.class
 *     Additional class: io/opentelemetry/javaagent/instrumentation/internal/lambda/Java9LambdaTransformer.class
 *     Additional class: io/opentelemetry/javaagent/instrumentation/internal/reflection/ReflectionHelper.class
 *
 *    Some virtual fields are not in the bootstrap class loader but int the agent class loader?
 *      java.lang.ClassNotFoundException: io/opentelemetry/javaagent/bootstrap/field/VirtualFieldImpl$java$sql$Connection$io$opentelemetry$javaagent$bootstrap$jdbc$DbInfo
 *
 */
public class AdditionalClassesInjectorListener implements HelperInjectorListener {

  @Override
  public void onInjection(Map<String, byte[]> classnameToBytes) {
    for (Map.Entry<String, byte[]> classEntry : classnameToBytes.entrySet()) {
      String classFileName = classEntry.getKey().replace(".", "/") + ".class";
      AdditionalClasses.put(classFileName, classEntry.getValue());
    }
  }
}
