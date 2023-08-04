plugins {
    `java-library`
    `hypo-java`
    `hypo-module`
}

dependencies {
    implementation(projects.hypoModel)
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 9
}

hypoModule {
    enableJavadoc = false
}
