plugins {
    java
    `hypo-java`
    `hypo-test-scenario-data`
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

tasks.compileScenario01Java {
    options.release.set(8)
}
