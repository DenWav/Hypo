import kotlin.math.max

plugins {
    `java-library`
}

dependencies {
    testImplementation(lib("junit-api"))
    testImplementation(lib("junit-params"))
    testRuntimeOnly(lib("junit-runtime"))
    testRuntimeOnly(lib("junit-launcher"))
}

tasks.test {
    useJUnitPlatform()

    maxParallelForks = max(Runtime.getRuntime().availableProcessors() / 2, 1)
}
