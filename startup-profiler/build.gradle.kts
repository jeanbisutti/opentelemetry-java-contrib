plugins {
  id("otel.java-conventions")
}

val jfrClient: Configuration by configurations.creating {
  isCanBeResolved = true
  isCanBeConsumed = false
}

dependencies {
  implementation("com.microsoft.jfr:jfr-streaming:1.2.0")
  jfrClient("com.microsoft.jfr:jfr-streaming:1.2.0")
}


tasks {
  jar {
    manifest {
      attributes(
        "Premain-Class" to "io.opentelemetry.contrib.startupprofiler.StartupProfilerAgent",
      )
    }
    inputs.files(jfrClient)
    from({
      jfrClient.singleFile
    })
  }
}