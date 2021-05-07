plugins {
    `java-library`
    `hypo-java`
    `hypo-module`
}

dependencies {
    implementation(projects.hypoMappings)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(9)
}

hypoModule {
    enableJavadoc.set(false)
}
