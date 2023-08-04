plugins {
    java
    `hypo-java`
    `hypo-test-scenario-data`
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
}

tasks.compileScenario01Java {
    options.release = 8
}
