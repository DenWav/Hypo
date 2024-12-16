plugins {
    java
    `hypo-java`
    `hypo-test-scenario-data`
}

// 21 by default
tasks.withType<JavaCompile>().configureEach {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
    options.release = 21
}

tasks.compileScenario01Java {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(11)
    }
    options.release = 11
}

tasks.compileScenario03Java {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
    options.release = 17
}
