plugins {
    java
    `hypo-java`
    `hypo-test-scenario-data`
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

tasks.compileScenario01Java {
    options.release = 11
}

tasks.compileScenario03Java {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
    options.release = 17
}

tasks.compileScenario12Java {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
    options.release = 21
}

