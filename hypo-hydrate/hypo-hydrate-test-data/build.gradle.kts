plugins {
    java
    `hypo-java`
    `hypo-test-scenario-data`
}

tasks.withType<JavaCompile>().configureEach {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
    options.release = 21
}
