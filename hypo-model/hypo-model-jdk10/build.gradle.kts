plugins {
    `java-library`
    `hypo-java`
    `hypo-module`
}

dependencies {
    implementation(projects.hypoModel)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(10)
}

hypoModule {
    enableJavadoc.set(false)
}
