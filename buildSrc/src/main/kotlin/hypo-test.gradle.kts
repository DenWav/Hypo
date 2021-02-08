plugins {
    `java-library`
}

dependencies {
    testImplementation(lib("junit-api"))
    testRuntimeOnly(lib("junit-runtime"))
}

tasks.test {
    useJUnitPlatform()
}
