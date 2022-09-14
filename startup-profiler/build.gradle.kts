plugins {
  id("otel.java-conventions")
}

dependencies {
  implementation("com.microsoft.jfr:jfr-streaming:1.2.0")
}


tasks {
  jar {
    manifest {
      attributes(
        "Premain-Class" to "io.opentelemetry.contrib.startupprofiler.StartupProfilerAgent",
      )
    }
  }
}