plugins {
    `java-library`
    `hypo-java`
    `hypo-module`
}

dependencies {
    implementation(projects.hypoModel)
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 10
}

hypoModule {
    enableJavadoc = false
}
